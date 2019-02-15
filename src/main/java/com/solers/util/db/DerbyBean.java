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

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.solers.util.LoggingOutputStream;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DerbyBean extends DatabaseBean {
    
    private static final int MAX_THREADS = 25;
    private static final Logger log = Logger.getLogger(DerbyBean.class);
    
    private final int port;
    private final NetworkServerControl control;
    private final Properties properties;
    private final AtomicBoolean started;
    private final String schemaName;
    private final String jdbcUrl;
    private final String encryptionKey;
    private final String userPassword;
    private final String adminPassword;
    
    /**
     * Derby uses reflection to call this method to get the appropriate stream
     * to write log messages to
     * @return LoggingOutputStream
     */
    public static OutputStream getLogStream() {
        return new LoggingOutputStream(log, Level.INFO);
    }
    
    public DerbyBean(File databaseDirectory, int port, String schemaName, String jdbcUrl, String encryptionKey, String userPassword, String adminPassword) throws Exception {
        setDatabaseDirectory(databaseDirectory);
        
        System.setProperty("derby.system.home", databaseDirectory.getAbsolutePath());
        System.setProperty("derby.connection.requireAuthentication", "true");
        System.setProperty("derby.database.sqlAuthorization", "true");
        System.setProperty("derby.authentication.provider", "BUILTIN");
        System.setProperty("derby.drda.sslMode", "peerAuthentication");
        System.setProperty("derby.stream.error.method", getClass()+".getLogStream");
        
        this.port = port;
        this.schemaName = schemaName;
        this.jdbcUrl = jdbcUrl;
        this.encryptionKey = encryptionKey;
        this.userPassword = userPassword;
        this.adminPassword = adminPassword;
        this.control = new NetworkServerControl(InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), port);
        this.properties = new Properties();
        this.started = new AtomicBoolean(false);
    }

    @Override
    protected boolean keepAlive() {
        return true;
    }

    @Override
    protected void doInitialize() {
        properties.setProperty("encryptionKey", encryptionKey);
        
        System.setProperty("derby.user.initialsa", adminPassword);
        
        properties.setProperty("create", "true");
        properties.setProperty("dataEncryption", "true");
        properties.setProperty("user", "initialsa");
        
        String [] statements = { 
            // The "normal" non-sa user is the same name as the schema
            "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.user."+schemaName+"', '"+userPassword+"')",
            "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.user.sa', '"+adminPassword+"')",
            "CREATE SCHEMA "+schemaName+" AUTHORIZATION sa",
            "GRANT EXECUTE ON PROCEDURE SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY TO sa"
        };
        
        execute(statements);
        
        properties.remove("create");
        properties.remove("dataEncryption");
        properties.setProperty("user", "sa");
        
        System.clearProperty("derby.user.initialsa");
    }

    @Override
    protected void doStart() {
        properties.setProperty("ssl", "peerAuthentication");
        properties.setProperty("password", adminPassword);
        properties.setProperty("encryptionAlgorithm", "AES/CBC/NoPadding");
        properties.setProperty("user", "sa");
        
        if (initialized()) {
            properties.setProperty("encryptionKey", encryptionKey);
        }
        
        try {
            control.start(new PrintWriter(new LoggingOutputStream(log, Level.INFO)));
        } catch (Exception ex) {
            log.error("Error starting DerbyDB", ex);
        }
        
        log.info("Derby listening on: "+port);
        started.set(true);
    }

    /**
     * We use a flag to check whether the database has been stopped because doStop()
     * can be called more than once depending on how the database was stopped.
     * 
     * In order to correctly shutdown, the database must be shutdown by the user
     * who created the database.  In order to do that, we have to keep the user/password
     * information around in memory.
     * 
     * If this becomes a problem, I'd suggest using @{code control.shutdown()} to "incorrectly"
     * shutdown the database without a user/password
     * 
     * @see com.solers.util.db.DatabaseBean#doStop()
     */
    @Override
    protected void doStop() {
        if (!started.compareAndSet(true, false)) {
            log.info("Derby already shutdown");
            return;
        }

        log.info("Stopping derby");
        System.setProperty("derby.user.initialsa", adminPassword);
        properties.setProperty("shutdown", "true");
        properties.setProperty("user", "initialsa");
        properties.setProperty("ssl", "peerAuthentication");
        properties.setProperty("password", adminPassword);
        try {
            Connection conn = getConnection();
            close(conn);
        } catch (SQLException ex) {
            log.info("DerbyDB shutdown  code: " + ex.getErrorCode() + "  sqlstate: " + ex.getSQLState());
        } finally {
            System.clearProperty("derby.user.initialsa");
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            return DriverManager.getConnection(jdbcUrl, properties);
        } catch (ClassNotFoundException ex) {
            log.error("Error finding database driver: " + ex.getMessage(), ex);
            throw new SQLException("Driver not found: ", ex);
        }
    }

    @Override
    protected int getPort() {
        return this.port;
    }
    
    /**
     * After the db is started, give the normal user select, update, insert and delete permissions
     * to all tables in the schema 
     * @see com.solers.util.db.DatabaseBean#onStart()
     */
    @Override
    protected void onStart() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        List<String> tables = new ArrayList<String>();
        
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select a.TABLENAME from sys.SYSTABLES a join sys.SYSSCHEMAS b on (a.SCHEMAID = b.SCHEMAID) where B.SCHEMANAME = '"+schemaName.toUpperCase()+"'");
            
            try {
                while (rs.next()) {
                    tables.add(rs.getString("TABLENAME"));
                }
            } finally {
                rs.close();
                close(stmt);
            }
            
            for (String table : tables) {
                String sql = String.format("GRANT SELECT, UPDATE, INSERT, DELETE ON %s.%s TO %s", schemaName.toUpperCase(), table, schemaName);
                stmt = conn.createStatement();
                stmt.execute(sql);
                close(stmt);
            }
            
        } catch (SQLException ex) {
            log.error("Error updating permissions", ex);
        } finally {
            close(stmt);
            close(conn);
            properties.clear();
        }
        
        try {
            control.setMaxThreads(MAX_THREADS);
        } catch (Exception ex) {
            log.error("Error setting max threads", ex);
        }
    }
}
