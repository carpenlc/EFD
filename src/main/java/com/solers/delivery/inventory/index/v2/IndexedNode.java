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
package com.solers.delivery.inventory.index.v2;

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

import com.solers.delivery.inventory.HashProperties;
import com.solers.delivery.inventory.index.HashUtil;
import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.HashFunction;

final class IndexedNode extends AbstractNode implements Serializable {
    private static final long serialVersionUID = 1L;
    protected static final Logger log = Logger.getLogger(IndexedNode.class);
    
    protected InventoryReader inventoryReader;
    private IndexedNode parent;
    protected transient EntryStruct node;
    private int offset;
    
    protected IndexedNode(InventoryReader inventoryReader, EntryStruct node, int offset) {
        this(inventoryReader, node, offset, null);
    }
    
    protected IndexedNode(InventoryReader inventoryReader, EntryStruct node, int offset, IndexedNode parent) {
        super(node.size);
        this.inventoryReader = inventoryReader;
        this.node = node;
        this.parent = parent;
        this.offset = offset;
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
    public InputStream openStream() throws IOException {
        return null;
    }
    
    @Override
    public long getTimestamp() {
        return node.lastModified;
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
    
    private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        node = inventoryReader.readEntry(offset);
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
            int offset = IndexedNode.this.node.children.get(index);
            try {
                EntryStruct newNode = inventoryReader.readEntry(offset);
                return new IndexedNode(inventoryReader, newNode, offset, IndexedNode.this);
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

    @Override
    public Node getChild(String name) {
        if (node.children.isEmpty()) return null;
        int index = HashUtil.index(
                name, 
                HashFunction.valueOf((String) inventoryReader.getProperty(HashProperties.ALGORITHM)), 
                node.children.size());
        int start = index;
        int offset = -1;
        try {
            EntryStruct child = null;
            while (index != start || offset == -1) {
                offset = node.children.get(index);
                child = inventoryReader.readEntry(offset);
                if (child != null) {
                    if (child.name.equals(name)) break;
                } else {
                    return null;
                }
                index++;
                index %= node.children.size();
            }
            if (child == null) return null;
            if (child.name.equals(name)) {
                return new IndexedNode(inventoryReader, child, offset, this);
            }
            return null;
        } catch (IOException ioe) {
            log.debug("Child " + name + " not found due to exception.", ioe);
            return null;
        }
    }
}
