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
import java.io.RandomAccessFile;
import java.util.List;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.Writer;
import com.solers.delivery.inventory.index.HashUtil;
import com.solers.delivery.inventory.index.HeaderStruct;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.HashFunction;

/**
 * The Indexer is responsible for generating an index file to a specific 
 * directory tree.  The index file has a very specific format, based off
 * the HeaderStruct and EntryStruct classes:
 * 
 * HEADER:
 * 2 bytes: marker (must be 0xEFD3)
 * 1 byte: file format version
 * 4 bytes: offset to inventory root node
 * 
 * BODY:
 * Following the header are <i>n</i> packed EntryStruct classes.  There
 * are two possible forms for EntryStruct, but the beginning part is shared:
 * <i>m</i> bytes: a UTF string containing the node name
 * 8 bytes: a long holding the size of the node
 * 8 bytes: a long holding the last modified timestamp of the node
 * 1 byte: a boolean indicating whether this is a leaf or an inner node.
 * 
 * If the current node is a LEAF, no further information is written and the next
 * node's information is written.  If the current node is an INNER NODE (e.g.
 * a directory, a node that can contain children) then <i>p</i> * 4 bytes are written
 * - one integer for each child offset (a pointer to the location within the file).
 * Finally, this list is terminated with an integer of value -1.  This tells a
 * parser to stop reading integers and end the current NODE's block.
 */
public final class InventoryWriter implements Writer {
    private static final HashFunction STANDARD_HASH = HashFunction.FNV;
    private static final byte FILE_FORMAT_VERSION = 2;
    private static final int  PLACEHOLDER = -1;
    
    private final Node rootDirectory;
    private final long timestamp;
    private HashFunction hash;
    
    public InventoryWriter(Node rootNode) {
        this(rootNode, STANDARD_HASH.name());
    }
    
    public InventoryWriter(Node rootNode, String hashFunction) {
        this(rootNode, hashFunction, 0);
    }
    
    public InventoryWriter(Node rootNode, String hashFunction, long timestamp) {
        if (rootNode == null)
            throw new IllegalArgumentException("Root node cannot be null.", new NullPointerException());
        if (!rootNode.isDirectory())
            throw new IllegalArgumentException("Root node must be a directory.");
        
        this.rootDirectory = rootNode;
        this.hash = HashFunction.valueOf(hashFunction);
        this.timestamp = timestamp;
    }
    
    /**
     * Create an inventory writer that will use the specified
     * location and the FNV hash algorithm.
     * @param rootDirectory
     */
    public InventoryWriter(Inventory backingInventory) {
        this(backingInventory, STANDARD_HASH.name());
    }
    
    /**
     * Create an inventory writer that will use the specified
     * location for creating the index, as well as a specific
     * hashing function.
     * @param rootDirectory the tree that will be indexed
     * @param hashFunction the name of the hash function to use.
     * @see com.solers.util.HashFunction
     */
    public InventoryWriter(Inventory backingInventory, String hashFunction) {
        this(backingInventory != null ? backingInventory.root() : null, hashFunction);
    }
    
    /**
     * Generate an index for this tree.  The file will be overwritten
     * if it exists.
     * @param indexFile the filename where the index will be saved
     * @return whether index generation was successful
     */
    public void write(String indexFile) throws IOException {
        write(new File(indexFile));
    }

    /**
     * Generate an index for this tree.  The file will be overwritten
     * if it exists.
     * @param index the file where the index will be saved
     * @return whether index generation was successful
     */
    public void write(File index) throws IOException {
        create(index);
        
        RandomAccessFile out = new RandomAccessFile(index, "rw");
        try {
            HeaderStruct header = HeaderStruct.create(FILE_FORMAT_VERSION, PLACEHOLDER);
            HeaderExtension v2Extension = HeaderExtension.create(hash.name(), PLACEHOLDER);
            header.pack(out);
            v2Extension.pack(out);

            int rootOffset = recurse(rootDirectory, out);
            
            out.seek(0);
            header = HeaderStruct.create(FILE_FORMAT_VERSION, rootOffset);
            v2Extension = HeaderExtension.create(hash.name(), timestamp == 0 ? System.currentTimeMillis() : timestamp);
            header.pack(out);
            v2Extension.pack(out);
        } finally {
            out.close();
        }
    }
    
    protected int recurse(Node n, RandomAccessFile out) throws IOException {
        //check for being interrupted
        if (Thread.currentThread().isInterrupted()) {
            throw new IOException("Could not write inventory due to interruption request.");
        }
        
        EntryStruct entry = EntryStruct.create(n);
        int[] hashTable;
        List<Node> children = n.getChildren();
        hashTable = new int[children.size()];
        for (Node child : children) {
            try {
                int offset = recurse(child, out);
                HashUtil.set(hashTable, HashUtil.index(child.getName(), hash, hashTable.length), offset);
            } catch (StackOverflowError soe) {
                throw new IOException("Could not write inventory due to extreme directory nesting.", soe);
            }
        }
        
        int offset = (int) out.getFilePointer();
        for (int i : hashTable) {
            entry.children.add(i);
        }
        entry.pack(out);
        return offset;
    }

    protected boolean create(File f) throws IOException {
        if (f.exists()) f.delete();
        return f.createNewFile();
    }
}
