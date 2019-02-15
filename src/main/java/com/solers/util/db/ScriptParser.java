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
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages SQL scripts and queries
 * 
 * @author dthemistokleous
 * 
 */
public class ScriptParser {
    
    /* KRJ 2016-10-24
    
    Converted FileReader object creation to "try with resources" in response to
    an HP Fortify issue identified.
    
    */
    
    public List<String> parse(File file) throws IOException {
        try (FileReader fileReader = new FileReader(file)) {
            return parse(new BufferedReader(fileReader));
        }
        catch (IOException ex) {
            throw new IOException(ex);
        }
    }
    
    public List<String> parse(Reader reader) throws IOException {
        List<String> result = new ArrayList<String>();
        
        String q = readNext(reader);
        while (!empty(q)) {
            result.add(q);
            q = readNext(reader);
        }
        
        reader.close();
        
        return result;
    }

    /**
     * Return a query read from a SQL file.
     * 
     * @param sb
     *            StringBuffer used to store a query.
     */
    private String readNext(Reader br) throws IOException {
        StringBuilder result = new StringBuilder();

        loop: for (;;) {
            int character = br.read();
            if (character == -1) {
                break;
            }
            
            switch (character) {
                case ';':
                case -1:
                    break loop;
                case '\n':
                case '\r':
                case '\t':
                    character = ' ';    
            }
            result.append((char) character); 
        }
        return result.toString().trim();
    }
    
    private boolean empty(String query) {
        return query == null || query.length() == 0;
    }
}
