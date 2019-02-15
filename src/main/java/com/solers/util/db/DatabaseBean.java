/****************************************************************
 *
 * Solers, Inc. as the author of Enterprise File Delivery 2.1 (EFD 2.1)
 * source code submitted herewith to the Government under contract
 * retains those intellectual property rights as set forth by the Federal 
 * Acquisition Regulations agreement (FAR). The Government has 
 * unlimited rights to redistribute copies of the EFD 2.1 in 
 * executable or source format to support operational installation 
 * and software maintenance. Additionally, the executable or 
 * source may be used or modified for by third parties as 
 * directed by the government.
 *
 * (c) 2009 Solers, Inc.
 ***********************************************************/
package com.solers.util.db;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import com.solers.util.db.action.DatabaseAction;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public abstract class DatabaseBean implements Database {
    
    protected static final Logger log = Logger.getLogger(DatabaseBean.class);
    
    private File databaseDirectory;
    private List<DatabaseAction> actions;

    public void setDatabaseDirectory(File databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    public void setActions(List<DatabaseAction> actions) {
        this.actions = actions;
    }

    public final void stop() {
        doStop();
    }
    
    public final void start() {
        doStart();
        if (!initialized()) {
            log.info("Database has not been initialized.  Running initialization...");
            initialize();
            log.info("Database initialized");
        }
        writeCurrentPort();
        
        for (DatabaseAction action : actions) {
            action.execute(this);
        }
        
        onStart();
        
        if (keepAlive()) {
            new Thread(new KeepAlive()).start();
        }
    }
    
    protected abstract int getPort();
    
    protected abstract void doStart();
    
    protected abstract void doInitialize();
    
    protected abstract void doStop();
    
    /**
     * Callback called when start is complete
     */
    protected void onStart() {
        
    }
    
    protected boolean keepAlive() {
        return false;
    }
    
    private void initialize() {
        doInitialize();
        try {
            getInitializationFile().createNewFile();
        } catch (IOException ex) {
            log.error("Error creating init file: " + ex.getMessage(), ex);
        }
    }
    
    protected final boolean initialized() {
        return getInitializationFile().exists();
    }
    
    private File getInitializationFile() {
        return new File(databaseDirectory, "init");
    }
    
    @Override
    public void execute(String... statements ) {
        Connection conn = null;
        try {
            conn = getConnection();
            for (String sql : statements) {
                Statement statement = conn.createStatement();
                try {
                    statement.execute(sql);
                } catch (SQLException ex) {
                    log.error("Error processing statement: " + ex.getMessage(), ex);
                } finally {
                    close(statement);
                }
            }
        } catch (SQLException ex) {
            log.error("Error processing statements: " + ex.getMessage(), ex);
        } finally {
            close(conn);
        }
    }
    
    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
                log.error("Error closing "+closeable+": " + ignore.getMessage(), ignore);
            }
        }
    }
    
    protected void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) {
                log.error("Error closing connection: " + ignore.getMessage(), ignore);
            }
        }
    }
    
    protected void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {
                log.error("Error closing statement: " + ignore.getMessage(), ignore);
            }
        }
    }
    
    private void writeCurrentPort() {
        File file = new File(databaseDirectory.getParentFile(), "port");
        if (file.exists()) {
            file.delete();
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(String.valueOf(getPort()));
        } catch (IOException ex) {
            log.error("Error writing current port", ex);
        } finally {
            close(writer);
        }
    }
    
    protected class KeepAlive implements Runnable {
        public void run() {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) {
                DatabaseBean.log.info("Interrupted, shutting down");
                DatabaseBean.this.stop();
                Thread.currentThread().interrupt();
            }
        }
    }
}
