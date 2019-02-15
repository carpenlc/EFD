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
package com.solers.delivery.inventory.index.v1;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.solers.delivery.inventory.Writer;
import com.solers.delivery.inventory.index.HeaderStruct;

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
    private static final byte FILE_FORMAT_VERSION = (byte) 1;
    private static final int  ROOT_OFFSET_PLACEHOLDER = -1;
    private final File rootDirectory;
    
    /**
     * Create an inventory writer that will use the specified
     * location for creating the index.
     * @param rootDirectory the tree that will be indexed
     */
    public InventoryWriter(File rootDirectory) {
        if (rootDirectory == null)
            throw new IllegalArgumentException("Root directory cannot be null.");
        if (!rootDirectory.exists())
            throw new IllegalArgumentException("Root directory must exist.");
        if (!rootDirectory.canRead())
            throw new IllegalArgumentException("Root directory must be readable.");
        if (!rootDirectory.isDirectory())
            throw new IllegalArgumentException("Root directory must be a directory/folder.");
        
        this.rootDirectory = rootDirectory;
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
            HeaderStruct header = HeaderStruct.create(FILE_FORMAT_VERSION, ROOT_OFFSET_PLACEHOLDER);
            header.pack(out);
            
            int rootOffset = recurse(rootDirectory, out);
            
            out.seek(0);
            header = HeaderStruct.create(FILE_FORMAT_VERSION, rootOffset);
            header.pack(out);
        } finally {
            out.close();
        }
    }
    
    protected int recurse(File f, RandomAccessFile out) throws IOException {
        //check for being interrupted
        if (Thread.currentThread().isInterrupted())
            throw new IOException("Could not write inventory due to interruption request.");
        
        EntryStruct entry = EntryStruct.create(f);
        
        File[] children = f.listFiles();
        if (children != null) {
            for (File child : children) {
                try {
                    int offset = recurse(child, out);
                    entry.children.add(offset);
                } catch (StackOverflowError soe) {
                    throw new IOException("Could not write inventory due to extreme directory nesting.", soe);
                }
            }
        }
        
        int offset = (int) out.getFilePointer();
        entry.pack(out);
        return offset;
    }

    protected boolean create(File f) throws IOException {
        if (f.exists()) f.delete();
        return f.createNewFile();
    }
}
