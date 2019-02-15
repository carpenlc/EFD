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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;
import com.solers.delivery.inventory.zip.util.EnumerationIterator;
import com.solers.delivery.inventory.zip.util.FilteredEnumerator;
import com.solers.delivery.inventory.zip.util.HierarchicalFilter;
import com.solers.util.Filter;

public class ZipFileRootNode extends AbstractNode {
    private final ZipFile zf;
    private final String rootName;
    private final long timestamp;
    
    private SoftReference<List<Node>> childrenRef = new SoftReference<List<Node>>(null);
    
    public ZipFileRootNode(ZipFile src, long timestamp, String rootName) {
        this.zf = src;
        this.timestamp = timestamp;
        this.rootName = rootName;
    }
    
    
    @Override
    public List<Node> getChildren() {
        List<Node> children = childrenRef.get();
        if (children == null) {
            children = new ArrayList<Node>();
            for (ZipEntry e : 
                EnumerationIterator.iterable(new FilteredEnumerator<ZipEntry>(zf.entries(), (Filter<ZipEntry>) new HierarchicalFilter(null)))) {
                    children.add(new ZipFileNode(this, rootName, this.timestamp, zf, e));
            }
            childrenRef = new SoftReference<List<Node>>(children);
        }
        return children;
    }
    
    @Override
    public Node getChild(String name) {
        return ZipFileNode.fromPath(getName(), getTimestamp(), zf, name);
    }

    @Override
    public String getName() {
        return rootName;
    }

    @Override
    public Node getParent() {
        return null;
    }

    @Override
    public String getPath() {
        return rootName;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public InputStream openStream() throws IOException {
        return null;
    }
}
