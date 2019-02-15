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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;
import com.solers.delivery.inventory.zip.util.EnumerationIterator;
import com.solers.delivery.inventory.zip.util.FilteredEnumerator;
import com.solers.delivery.inventory.zip.util.HierarchicalFilter;
import com.solers.util.Filter;

public class ZipFileNode extends AbstractNode {
    private final Node parent;
    private final ZipFile source;
    private final ZipEntry entry;
    private final String rootName;
    private final long rootTimestamp;
    
    private SoftReference<List<Node>> childrenRef = new SoftReference<List<Node>>(null);
    
    public ZipFileNode(Node parent, String rootName, long rootTimestamp, ZipFile source, ZipEntry entry) {
        super(entry.getSize());
        this.parent = parent;
        this.source = source;
        this.entry = entry;
        this.rootName = rootName;
        this.rootTimestamp = rootTimestamp;
    }
    
    public static ZipFileNode fromPath(String rootName, long rootTimestamp, ZipFile source, final String path) {
        ZipEntry entry = null;
        //we must try ending with / first to circumvent bug
        //in zipfile where directories are read as files.
        if (!path.endsWith("/")) {
            entry = source.getEntry(path + "/");
        }
        if (entry == null) {
            entry = source.getEntry(path);
        }
        if (entry == null) return null;
        
        String parent = getParentPath(entry.getName());
        Node parentNode = null;
        if (parent == null) {
            parentNode = new ZipFileRootNode(source, rootTimestamp, rootName);
        } else {
            parentNode = fromPath(rootName, rootTimestamp, source, parent);
        }
        return new ZipFileNode(parentNode, rootName, rootTimestamp, source, entry);
    }
    
    public static String getParentPath(final String currentPath) {
        String path = currentPath;
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        if (path.contains("/")) {
            return path.substring(0, path.lastIndexOf("/"));
        }
        return null;
    }
    
    @Override
    public List<Node> getChildren() {
        if (!isDirectory()) return Collections.emptyList();
        
        List<Node> children = childrenRef.get();
        if (children == null) {
            children = new ArrayList<Node>();
            for (ZipEntry e : 
                EnumerationIterator.iterable(new FilteredEnumerator<ZipEntry>(source.entries(), (Filter<ZipEntry>) new HierarchicalFilter(entry.getName())))) {
                    children.add(new ZipFileNode(this, rootName, rootTimestamp, source, e));
            }
            childrenRef = new SoftReference<List<Node>>(children);
        }
        return children;
    }
    
    @Override
    public Node getChild(String name) {
        if (!isDirectory()) return null;
        return ZipFileNode.fromPath(rootName, rootTimestamp, source, entry.getName() + name);
    }

    @Override
    public String getName() {
        String path = getPath();
        return path.substring(path.lastIndexOf("/") + 1); 
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public String getPath() {
        String path = entry.getName();
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return rootName + "/" + path;
    }

    @Override
    public long getTimestamp() {
        return entry.getTime();
    }

    @Override
    public boolean isDirectory() {
        return entry.isDirectory();
    }

    @Override
    public InputStream openStream() throws IOException {
        if (isDirectory()) return null;
        return source.getInputStream(entry);
    }

}
