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
package com.solers.util.db.action;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.solers.util.db.Database;
import com.solers.util.db.SqlChangeSet;
import com.solers.util.db.SqlChangeSetManager;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SqlChangesetAction implements DatabaseAction {
    
    private static final Logger log = Logger.getLogger(SqlChangesetAction.class);
    
    private File databaseDirectory;
    private File sqlConfig;
    
    public void setDatabaseDirectory(File databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    public void setSqlConfig(File sqlConfig) {
        this.sqlConfig = sqlConfig;
    }
    
    @Override
    public void execute(Database db) {
        try {
            SqlChangeSetManager manager = new SqlChangeSetManager(sqlConfig, getVersionFile());
            List<SqlChangeSet> changeSets = manager.getChangeSets();
            for (SqlChangeSet changeSet : changeSets) {
                log.info("Processing changeset: "+changeSet.getVersion());
                db.execute(changeSet.getStatements().toArray(new String[]{}));
            }
            writeVersion(db, manager.getCurrentVersion());
        } catch (IOException ex) {
            log.error("I/O error processing sql changesets", ex);
        }
    }
    
    private void writeVersion(Database db, String version) {
        File file = getVersionFile();
        if (file.exists()) {
            file.delete();
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(version);
        } catch (IOException ex) {
            log.error("Error writing version", ex);
        } finally {
            close(writer);
        }
    }
    
    private File getVersionFile() {
        return new File(databaseDirectory, "version");
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

}
