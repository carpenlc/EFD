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

import java.util.List;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SqlChangeSet implements Comparable<SqlChangeSet> {
    
    private final String version;
    private final List<String> statements;
    
    public SqlChangeSet(String version, List<String> statements) {
        this.version = version;
        this.statements = statements;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getStatements() {
        return statements;
    }

    @Override
    public int compareTo(SqlChangeSet o) {
        return version.compareTo(o.version);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SqlChangeSet) {
            return compareTo((SqlChangeSet)obj) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return version.hashCode();
    }

    /**
     * @param version
     * @return True if this changeset is newer than {@code version}
     */
    public boolean newerThan(String version) {
        return getVersion().compareTo(version) > 0;
    }
}
