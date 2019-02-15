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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SqlChangeSetManager {
    
    private final String version;
    private final List<SqlChangeSet> allChangeSets;
    
    public SqlChangeSetManager(File sqlConfig, File versionFile) throws IOException {
        allChangeSets = new SqlChangeSetParser().parse(sqlConfig);
        version = readVersion(versionFile);
    }
    
    public String getCurrentVersion() {
        SqlChangeSet changeSet = allChangeSets.get(allChangeSets.size() - 1);
        return changeSet.getVersion();
    }

    public List<SqlChangeSet> getChangeSets() {
        if (version == null || version.length() == 0) {
            return allChangeSets;
        }
        List<SqlChangeSet> result = new ArrayList<SqlChangeSet>();
        
        for (SqlChangeSet s : allChangeSets) {
            if (s.newerThan(version)) {
                result.add(s);
            }
        }
        
        return result;
    }
    
    private String readVersion(File versionFile) throws IOException {
        if (versionFile == null || !versionFile.exists() || !versionFile.canRead()) {
            return null;
        }
        
        BufferedReader reader = new BufferedReader(new FileReader(versionFile));
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }
}
