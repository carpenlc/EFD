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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.solers.delivery.inventory.comparer.NodeListener;
import com.solers.delivery.inventory.index.HashUtil;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.HashFunction;

/**
 * The Difference File Event Handler is equivalent to an inventory writer.
 * It receives the difference events from a Comparer and writes them out
 * into a file.
 * 
 * Use the instance method getDifferenceCount to retrieve the number of 
 * differences were found, if any, if you wish to make decisions based on
 * the presence or absence of differences in the file system.
 */
public final class DifferenceFileEventHandler implements NodeListener {
    public static final long TIME_OF_COMPLETION = 0;
    public static final HashFunction DEFAULT_HASH = HashFunction.FNV;
    
    private static final Logger log = Logger.getLogger(DifferenceFileEventHandler.class);
    
    private static final byte VERSION = 1;
    private static final byte PLACEHOLDER = -1;
    
    private final File differenceFile;
    private final RandomAccessFile out;
    private final HashFunction hash;
    private final long timestamp;
    
    private int differenceCount = 0;
    
    private HeaderStruct header = null;
    /*
     * The relationshipMap maps a parent path to children and offsets
     * for example
     * <some/path/with/diffs, <updatedNodeName, offset>>
     */
    private Map<String, Map<String, Integer>> relationshipMap;
    
    public DifferenceFileEventHandler(File differenceFile) {
        this(differenceFile, DEFAULT_HASH.name());
    }
    
    public DifferenceFileEventHandler(File differenceFile, String algorithm) {
        this(differenceFile, algorithm, TIME_OF_COMPLETION);
    }
    
    /**
     * Create a difference file with the specified hashing algorithm and timestamp.
     * If timestamp is set to 0, the completion time will be written into the difference
     * file.
     * @param differenceFile the file where the data will be written
     * @param algorithm the name of the hash algorithm to use
     * @param timestamp 0 for completion time, or a specific timestamp to write to the header
     */
    public DifferenceFileEventHandler(File differenceFile, String algorithm, long timestamp) {
        this.differenceFile = differenceFile;
        this.hash = HashFunction.valueOf(algorithm);
        this.timestamp = timestamp;
        
        try {
            create(this.differenceFile);
            this.out = new RandomAccessFile(this.differenceFile, "rw");
        } catch (FileNotFoundException fnfe) {
            throw new IllegalArgumentException(fnfe);
        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe);
        }
    }
    
    /**
     * Write an added node to the difference table.  Will recurse to include any new 
     * children as well.
     * @param node
     * @param recurse
     */
    @Override
    public void onAdd(Node node) {
        onChange(node, true);
    }
    
    /**
     * Write this change to the difference file, recursing if instructed by the calling
     * code.
     * @param node the node (and possible children) that differ.
     * @param recurse recurse the node's children if applicable to this change.
     */
    private void onChange(Node node, boolean recurse) {
        try {
            EntryStruct entry = EntryStruct.create(node, true, false);
            if (entry.isDirectory && recurse) {
                addChildren(node, entry);
            }
            prepare(node);
            entry.pack(out);
            differenceCount++;
        } catch (IOException ioe) {
            log.debug("Failed to add difference", ioe);
        }
    }
    
    private void addChildren(Node n, EntryStruct entry) {
        for (Node child : n.getChildren()) {
            EntryStruct childEntry = EntryStruct.create(child, true, true);
            if (child.isDirectory()) {
                addChildren(child, childEntry);
            }
            try {
                entry.children.add((int) out.getFilePointer());
                childEntry.pack(out);
                differenceCount++;
            } catch (IOException ioe) {
                log.debug("Failed to add children", ioe);
            }
        }
    }

    /**
     * Write a removed node to the difference file.
     */
    @Override
    public void onRemove(Node node) {
        try {
            EntryStruct entry = EntryStruct.create(node, false, false);
            prepare(node);
            entry.pack(out);
            differenceCount++;
        } catch (IOException ioe) {
            log.debug("Failed to add removed node", ioe);
        }
    }
    
    /**
     * Takes the current node and checks its parent for relationships.
     * If this is the first time we have encountered the parent node, an entry
     * is made for it in the relationship map.  Otherwise, its map of children
     * and offsets is loaded.  The offset to the new entry is recorded to be
     * written later to the root/diff element relationship table.
     * @param node the node that differs
     */
    private void prepare(Node node) throws IOException {
        Node parent = node.getParent();
        String parentPath = (parent == null) ? HeaderStruct.ROOT_PARENT : parent.getPath();
        Map<String, Integer> offsets;
        if (relationshipMap.containsKey(parentPath)) {
            offsets = relationshipMap.get(parentPath);
        } else {
            offsets = new HashMap<String, Integer>();
            relationshipMap.put(parentPath, offsets);
        }
        offsets.put(node.getName(), (int) out.getFilePointer());
    }

    /**
     * The start method creates the initial header with place holder values
     * and initializes data structures to keep tabs on node relationships, as
     * differences are found in the tree.
     */
    @Override
    public void onStart() {
        header = HeaderStruct.create(VERSION, PLACEHOLDER, hash.name(), PLACEHOLDER);
        relationshipMap = new HashMap<String, Map<String, Integer>>();
        
        try {
            out.seek(0);
            header.pack(out);
        } catch (IOException ioe) {
            log.debug("Failed to write header.", ioe);
        }
    }

    /**
     * Scanning for differences has completed.  The onStop() method will write out the
     * remaining two body sections: the ROOT/DIFFERENCE ELEMENT RELATIONSHIP TABLE, and
     * then finally the ROOT ELEMENTS LOOKUP TABLE.  Since the HEADER requires a pointer
     * to the ROOT ELEMENTS LOOKUP TABLE, this method will also fill any place holder
     * values in the header.
     */
    @Override
    public void onStop() {
        try {
            //move to end of file as a sanity check
            out.seek(out.length());
            
            //write parents and all their modified children out to disk,
            //saving the offset for the parent offset index.  Use array
            //for hashing parent offset table entries.  This is the
            //ROOT/DIFFERENCE ELEMENTS RELATIONSHIP TABLE
            int[] parentTable = new int[relationshipMap.size()];
            for (Entry<String, Map<String, Integer>> e : relationshipMap.entrySet()) {
                int parentOffset = (int) out.getFilePointer();
                String normalizedPath = e.getKey().replace(File.separator, HeaderStruct.SEPARATOR);
                Map<String, Integer> childMap = e.getValue();
                int[] children = new int[childMap.size()];
                
                HashUtil.set(parentTable, HashUtil.index(normalizedPath, hash, parentTable.length), parentOffset);
                
                for (Entry<String, Integer> childEntry : childMap.entrySet()) {
                    HashUtil.set(children, HashUtil.index(childEntry.getKey(), hash, children.length), childEntry.getValue());
                }
                
                out.writeUTF(normalizedPath);
                out.writeInt(children.length);
                for (int i : children) {
                    out.writeInt(i);
                }
            }
            
            //write the parent offset table and save the pointer so
            //that the header can point directly to it.  this is the
            //ROOT ELEMENTS LOOKUP TABLE
            int tableOffset = (int) out.getFilePointer();
            out.writeInt(parentTable.length);
            for (int i : parentTable) {
                out.writeInt(i);
            }
            
            //seek to beginning of file and rewrite the header with pointer
            //to the final location of the parent offset table.  this is the
            //HEADER
            out.seek(0);
            header = HeaderStruct.create(VERSION, timestamp == 0 ? System.currentTimeMillis() : timestamp, hash.name(), tableOffset);
            header.pack(out);
        } catch (IOException ioe) {
            log.error("Could not write difference file.", ioe);
        } finally {        
            try {
                if (out != null) out.close();
            } catch (IOException ioe) {
                log.debug("Failed to close diff file output.", ioe);
            }
        }
    }

    /**
     * Updates are semantically the same as adds when trying to store the differences in
     * an inventory file.  Both events mean that we must write the node into the difference
     * file.  The difference file itself only distinguishes between "same," (not written to
     * difference file) "removed," and "changed." (An update being a change.)  The update
     * instructs the private onChange method to recurse if the node is a directory, since
     * an "updated" directory essentially means a transition from a file to a directory.
     * This is because time stamp and size data is ignored for a directory, so the typical
     * directory actions are only add and remove.
     */
    @Override
    public void onUpdate(Node node) {
        onChange(node, true);
    }
    
    /**
     * Retrieve the number of differences that were found, 0 for none.
     * @return the number of differences in the inventory file.
     */
    public int getDifferenceCount() {
        return differenceCount;
    }
    
    protected boolean create(File f) throws IOException {
        if (f.exists()) f.delete();
        return f.createNewFile();
    }
}
