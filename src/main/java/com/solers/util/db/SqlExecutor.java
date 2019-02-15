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

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.solers.util.IOConsole;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public abstract class SqlExecutor {
    
    protected final PrintStream ps;
    
    private Connection connection;
    
    public SqlExecutor(PrintStream ps) {
        this.ps = ps;
    }
    
    public SqlExecutor() {
        this(System.out);
    }
    
    public void loop() {
        IOConsole.DEFAULT.println("Type \"help\" for help or \"quit\" to quit");
        while (true) {
            String input = IOConsole.DEFAULT.readLine("sql");
            if (input.equals("quit")) {
                break;
            }
            
            try {
                execute(input);
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }
        try {
            close();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public void execute(String sql) throws SQLException {
        if (sql == null || sql.length() == 0) {
            return;
        }
        
        String trimmed = sql.trim();
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        
        if (trimmed.equals("help")) {
            doHelp();
        } else {
            doExecute(trimmed);
        }
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = createConnection();
        }
        return connection;
    }
    
    public void close() throws SQLException {
        getConnection().close();
    }
    
    /**
     * Subclasses should override this to provide support for meta commands
     * 
     * @param sql
     * @throws SQLException
     */
    protected void doExecute(String sql) throws SQLException {
        Statement stmt = getConnection().createStatement();
        try {
            boolean hasResultSet = stmt.execute(sql);
            if (hasResultSet) {
                printResultSet(stmt.getResultSet());
            } else {
                int count = stmt.getUpdateCount();
                if (count > 0) {
                    ps.println(count+" records affected");
                }
            }
        } finally {
            close(stmt);
        }
    }
    
    /**
     * Subclasses can provide their own commands
     */
    protected void doHelp() {
        ps.println("Available commands:");
    }
    
    @Override
    protected void finalize() {
        try {
            close();
        } catch (SQLException ignore) {
            
        }
    }

    private void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        String largest = "";
        List<String> columns = new ArrayList<String>(count);
        
        for (int i=1; i <= count; i++) {
            String name = meta.getColumnName(i);
            if (name.length() > largest.length()) {
                largest = name;
            }
            columns.add(name);
        }
        
        int len = largest.length() + 1;
        for (String column : columns) {
           printPadded(column, len); 
        }
        ps.println();
        
        for (int i=0; i < count*len; i++) {
            ps.print('-');
        }
        ps.println();
        
        while (rs.next()) {
            for (int i=1; i<= count; i++) {
                printPadded(rs.getString(i), len);
                ps.print(' ');
            }
            ps.println();
        }
        ps.flush();
        close(rs);
    }
    
    private void printPadded(String str, int length) {
        int strlen = str == null ? 0 : str.length();
        int extra = length - strlen;
        
        ps.print(str);
        for (int i=0; i < extra; i++) {
            ps.print(' ');
        }
    }
    
    protected void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {
                
            }
        }
    }
    
    protected void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
                
            }
        }
    }
    
    protected abstract Connection createConnection() throws SQLException;
}
