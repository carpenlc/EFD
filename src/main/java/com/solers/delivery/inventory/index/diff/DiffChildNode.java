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
package com.solers.delivery.inventory.index.diff;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;

/**
 * The DiffChildNode represents an actual difference in the inventory.  Its parent
 * node is a help to the merging algorithm, so that all elements of an inner node
 * can be collected and pointed to.
 * 
 * The DiffChildNode knows whether its entry has been removed or changed.  A change
 * is the same as an add or an update, as far as the inventory is concerned.  Whether
 * a "change" is an add or an update depends on the inventory this merges into or was
 * taken from.
 */
final class DiffChildNode extends AbstractNode implements DifferenceNode, Serializable {
    private static final long serialVersionUID = 1L;
    protected static final Logger log = Logger.getLogger(DiffChildNode.class);
    
    protected DifferenceFileReader diffReader;
    private Node parent;
    protected transient EntryStruct node;
    private int offset;
    private boolean child;
    
    protected DiffChildNode(DifferenceFileReader diffReader, EntryStruct node, int offset) {
        this(diffReader, node, offset, null);
    }
    
    protected DiffChildNode(DifferenceFileReader diffReader, EntryStruct node, int offset, Node parent) {
        super(node.size);
        this.diffReader = diffReader;
        this.node = node;
        this.parent = parent;
        this.offset = offset;
        this.child = node.child;
    }
    
    @Override
    public String getName() {
        return node.name;
    }
    
    @Override
    public String getPath() {
        if (parent != null) {
            return parent.getPath() + File.separator + getName();
        } else {
            return getName();
        }
    }
    
    @Override
    public boolean isDirectory() {
        return node.isDirectory;
    }
    
    @Override
    public long getTimestamp() {
        return node.lastModified;
    }
    
    @Override
    public boolean isChanged() {
        return node.changed;
    }
    
    @Override
    public boolean isRemoved() {
        return !node.changed;
    }
        
    @Override
    public List<Node> getChildren() {
        int children = node.children.size();
        if (children == 0) {
            return Collections.emptyList();
        } else {
            return new LazyList(children);
        }
    }
    
    @Override
    public Node getParent() {
        return parent;
    }
    
    @Override
    public InputStream openStream() {
        return null;
    }
    
    private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        node = diffReader.readEntry(offset, child);
    }
    
    private void writeObject(ObjectOutputStream stream)
    throws IOException {
        stream.defaultWriteObject();
    }
    
    private final class LazyList extends AbstractList<Node> {
        private static final long serialVersionUID = 3661890726972677055L;
        
        private final int size;
        private final Map<Integer, Node> remap;
        
        protected LazyList(int size) {
            super();
            this.size = size;
            remap = new HashMap<Integer, Node>(size);
        }
        
        @Override
        public int size() {
            return size;
        }
        
        @Override
        public Node get(int index) {
            if (remap.containsKey(index)) return remap.get(index);
            int offset = DiffChildNode.this.node.children.get(index);
            try {
                EntryStruct newNode = diffReader.readEntry(offset, true);
                return new DiffChildNode(diffReader, newNode, offset, DiffChildNode.this);
            } catch (IOException ioe) {
                log.error("Error retrieving child node index: " + index, ioe);
                return null;
            }
        }
        
        @Override
        public Node set(int index, Node n) {
            Node currValue = get(index);
            remap.put(index, n);
            return currValue;
        }
    }
}
