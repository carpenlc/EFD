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
package com.solers.delivery.inventory.zip;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.node.FilteredNode;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.Filter;

public class ZipFileInventory implements Inventory {
    private final ZipFile zf;
    private final File source;
    private final String rootName;
    private Filter<Node> filter = null;
    
    private static final String ARCHIVE_SIZE = "archive.size";
    
    private static final Set<String> properties = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(ARCHIVE_SIZE)));
    
    public ZipFileInventory(File archive, String rootName) {
        try {
            zf = new ZipFile(archive, ZipFile.OPEN_READ);
            this.source = archive;
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Expected a zip archive", ioe);
        }

        this.rootName = getRootName(archive.getName(), rootName);
    }
    
    protected String getRootName(final String file, final String root) {
        String finalRootName = root;
        if (finalRootName == null || finalRootName.trim().equals("")) {
            finalRootName = file;
            int dot = finalRootName.lastIndexOf('.');
            if (dot > 0) finalRootName = new String(
                finalRootName.substring(0, dot));
        }
        return finalRootName;
    }
    
    
    @Override
    public void close() {
        try {
            zf.close();
        } catch (IOException ioe) {
            //don't care
        }
    }

    @Override
    public Object getProperty(String name) {
        if (ARCHIVE_SIZE.equals(name)) {
            return source.length();
        }
        return null;
    }

    @Override
    public String getRootName() {
        return this.rootName;
    }

    @Override
    public Set<String> properties() {
        return properties;
    }

    @Override
    public Node read(final String path) {
        //chop the rootName off, since this is an abstraction
        //necessary to the EFD single-root inventory system.
        if (!path.startsWith(rootName + "/")) return null;
        String searchPath = path.substring(rootName.length() + 1);
        return FilteredNode.applyFilter(ZipFileNode.fromPath(
            rootName, source.lastModified(), zf, searchPath), filter);
    }

    @Override
    public Node root() {
        return FilteredNode.applyFilter(
            new ZipFileRootNode(zf, source.lastModified(), rootName),
            filter);
    }

    @Override
    public void useFilter(Filter<Node> filter) {
        this.filter = filter;
    }

}
