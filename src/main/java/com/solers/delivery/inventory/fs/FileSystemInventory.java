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
package com.solers.delivery.inventory.fs;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.node.FilteredNode;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.Filter;

public class FileSystemInventory implements Inventory {
    private final File root;
    private Filter<Node> filter = null;
    
    public FileSystemInventory(String root) {
        this(new File(root));
    }
    
    public FileSystemInventory(File root) {
        if (root == null) {
            throw new IllegalArgumentException("Inventory root cannot be null.");
        }
        
        if (root.exists() && root.isFile()) {
            throw new IllegalArgumentException("Inventory root must be a directory.");
        }
        
        String canonicalName = root.getName();
        try {
            canonicalName = root.getCanonicalFile().getName();
        } catch (IOException ioe) {
            //don't care
        }
        
        //Fix issue on Windows where actual path name case different from possible input
        File f = new File(FilenameUtils.normalizeNoEndSeparator(root.getAbsolutePath()));
        if (canonicalName.toLowerCase().equals(root.getName().toLowerCase())) {
            f = new File(f.getParentFile(), canonicalName);
        }
        
        this.root = f;
    }
    
    @Override
    public Node root() {
        if (!root.exists()) return null;
        return FilteredNode.applyFilter(new FileNode(root), filter);
    }
    
    @Override
    public String getRootName() {
        return root.getName();
    }
    
    @Override
    public Node read(String path) {
        File f = new File(root.getParentFile(), path);
        if (!f.exists()) return null;
        return FilteredNode.applyFilter(new FileNode(f, root), filter);
    }
    
    @Override
    public void useFilter(Filter<Node> filter) {
        this.filter = filter;
    }
    
    /**
     * No op
     * @see java.io.Closeable#close()
     */
    public void close() {
        
    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public Set<String> properties() {
        return Collections.emptySet();
    }
}
