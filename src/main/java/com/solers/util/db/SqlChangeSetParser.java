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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses a sql change set file.  The file format is as follows
 * 
 * <pre>
 * 
 * # A comment
 * BEGIN-CHANGESET <version>
 *   sql stament;
 * END-CHANGESET
 * 
 * # Another comment
 * 
 * BEGIN-CHANGESET <new-version>
 *   create table ...;
 *   insert into ...;
 * END-CHANGESET
 * </pre>
 * 
 * After BEGIN-CHANGESET, the parser treats the rest of the line (minus leading/trailing whitespace)
 * as the version number.  Version numbers should increment in some meaningful way such that
 * changesets can be compared against one another
 * 
 * Comments can be put in between changeset definitions, but not in the change sets
 * themselves.
 * 
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SqlChangeSetParser {
    
    private static final String BEGIN_MARKER = "BEGIN-CHANGESET";
    private static final String END_MARKER = "END-CHANGESET";
    
    private final ScriptParser scriptParser = new ScriptParser();
    
    public List<SqlChangeSet> parse(File file) throws IOException {
        List<SqlChangeSet> result = new ArrayList<SqlChangeSet>();  
                
        // KRJ 2016-08-31: Converted reader creation to "try with resources"
        // based on an HP Fortify recommendation
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith(BEGIN_MARKER)) {
                    String version = getVersion(line);
                    result.add(new SqlChangeSet(version, readStatements(reader)));
                }
                line = reader.readLine();
            }
            
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
                
        
        Collections.sort(result);
        
        return result;
    }
    
    private String getVersion(String versionLine) {
        return versionLine.substring(BEGIN_MARKER.length() + 1).trim(); // +1 to account for space
    }
    
    private List<String> readStatements(BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(END_MARKER)) {
                break;
            }
            builder.append(line);
            line = reader.readLine();
        }
        if (line == null) {
            throw new IOException("EOF reached before "+END_MARKER);
        }
        return scriptParser.parse(new StringReader(builder.toString()));
    }
    
}
