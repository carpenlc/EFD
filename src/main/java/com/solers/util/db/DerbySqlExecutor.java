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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DerbySqlExecutor extends SqlExecutor {
    
    private final String jdbcUrl;
    private final Properties jdbcProperties;
    
    public DerbySqlExecutor(String jdbcUrl, Properties jdbcProperties) throws SQLException {
        this.jdbcUrl = jdbcUrl;
        this.jdbcProperties = jdbcProperties;
    }

    @Override
    protected void doExecute(String sql) throws SQLException {
        String lower = sql.toLowerCase();
        if (lower.startsWith("desc")) {
            executeDescribe(sql);
        } else if (lower.startsWith("show tables")){
            executeShowTables();
        } else {
            super.doExecute(sql);
        }
    }
    
    @Override
    protected void doHelp() {
        super.doHelp();
        ps.println("show tables - List the tables in the database");
        ps.println("desc <tablename> - Describe the table <tablename>");
    }

    private void executeShowTables() throws SQLException {
        super.doExecute("select s.schemaname||'.'||t.tablename as \"SCHEMANAME.TABLENAME\" from sys.sysschemas s, sys.systables t where s.schemaid = t.schemaid");
    }
    
    private void executeDescribe(String sql) throws SQLException {
        String tableName = sql.substring("desc ".length()).toUpperCase();
        super.doExecute(String.format("SELECT a.COLUMNNAME, a.COLUMNDATATYPE FROM sys.SYSCOLUMNS a JOIN sys.SYSTABLES b on (a.REFERENCEID = b.TABLEID) WHERE UPPER(b.TABLENAME) = '%s' ORDER BY a.COLUMNNUMBER ASC", tableName));
    }

    @Override
    protected Connection createConnection() throws SQLException { 
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("Derby driver cannot be found", ex);
        }
        
        return DriverManager.getConnection(jdbcUrl, jdbcProperties);
    }
}
