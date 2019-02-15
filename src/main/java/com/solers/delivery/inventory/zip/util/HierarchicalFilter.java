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
package com.solers.delivery.inventory.zip.util;

import java.util.zip.ZipEntry;

import com.solers.util.Filter;

public class HierarchicalFilter implements Filter<ZipEntry>{
    final String path;
    
    public HierarchicalFilter(String path) {
        this.path = path;
    }
    
    @Override
    public boolean accept(ZipEntry entry) {
        if (path == null) {
            return inDirectory(entry.getName());
        } else {
            //parse out shared path names and treat
            //new name as though it is root, using
            //same logic
            String name = entry.getName();
            if (name.length() < path.length()) return false;
            if (!name.startsWith(path)) return false;
            String newRoot = name.substring(path.length());
            if (newRoot.trim().equals("")) return false;
            return inDirectory(newRoot);
        }
    }

    /*
     * Name either does not contain /, or if it
     * does contain /, it contains at most 1 which
     * occurs only at the end of the path.
     */
    protected boolean inDirectory(String name) {
        return ((name.indexOf("/", 0) == name.lastIndexOf("/"))
            && name.endsWith("/"))
            || !name.contains("/");
    }
}
