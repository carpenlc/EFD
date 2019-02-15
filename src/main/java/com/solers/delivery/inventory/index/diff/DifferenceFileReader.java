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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.solers.delivery.inventory.DifferenceInventory;
import com.solers.delivery.inventory.HashProperties;
import com.solers.delivery.inventory.TimeProperties;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.Filter;
import com.solers.util.HashFunction;

public final class DifferenceFileReader implements DifferenceInventory, Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger log = Logger.getLogger(DifferenceFileReader.class);
    private static final String[] PROP_ARRAY = new String[] { 
        HashProperties.ALGORITHM, 
        TimeProperties.TIMESTAMP };
    private static final Set<String> properties = new HashSet<String>(Arrays.asList(PROP_ARRAY));
    private static final int VERSION = 1;
    
    private transient RandomAccessFile in;
    private transient HashFunction hash;
    private transient long lkg;
    private transient int diffCount;
    private transient List<Integer> diffTable;
    private transient Map<String, Node> rootCache;
    
    private final File sourceFile;
    
    public DifferenceFileReader(File differenceFile) {
        this.sourceFile = differenceFile;
        initialize();
    }
    
    private void initialize() {
        //RandomAccessFile will throw suitable exceptions if
        //we try to use a nonexistent or unreadable file
        try {
            this.in = new RandomAccessFile(sourceFile, "r");
        } catch (FileNotFoundException fnfe) {
            throw new IllegalArgumentException(fnfe);
        }
        
        try {
            //load and validate header
            HeaderStruct header = HeaderStruct.create(in);
            if (header == null) {
                throw new IllegalArgumentException("Could not read inventory difference file.");
            }
            if (header.version != VERSION) {
                throw new IllegalArgumentException(
                    String.format("Cannot read difference file version %d (expected %d).",
                        header.version, VERSION));
            }
            
            //read metadata from header, such as timestamp and hash algorithm
            this.hash = HashFunction.valueOf(header.hash);
            this.lkg = header.lkg;
            
            //store information about the table of root changes
            in.seek(header.offset);
            diffCount = in.readInt();
            diffTable = new ArrayList<Integer>(diffCount);
            for (int i = 0; i < diffCount; ++i) {
                diffTable.add(in.readInt());
            }
            rootCache = new HashMap<String, Node>(diffTable.size());
            cacheRoots();
        } catch (IOException ioe) {
            close();
            throw new IllegalArgumentException(ioe);
        }
    }
    
    public long timestamp() {
        return lkg;
    }
    
    public HashFunction getHash() {
        return hash;
    }
    
    @Override
    public Node root() {
        return new DiffRootNode(this);
    }
    
    @Override
    public String getRootName() {
        return HeaderStruct.ROOT_PARENT;
    }
    
    protected Collection<Node> roots() {
        return rootCache.values();
    }
    
    protected void cacheRoots() {
        for (int offset : diffTable) {
            try {
                Node n = readRoot(offset);
                rootCache.put(n.getPath(), n);
            } catch (IOException ioe) {
                log.debug("Read failure getting difference roots", ioe);
            }
        }
        
    }
    
    @Override
    public Node read(String path) {
        return rootCache.get(path);
    }
    
    protected DiffParentNode readRoot(int offset) throws IOException {
        in.seek(offset);
        String name = in.readUTF();
        int count = in.readInt();
        int[] children = new int[count];
        for (int i = 0; i < count; ++i) {
            children[i] = in.readInt();
        }
        return new DiffParentNode(this, name.replace(HeaderStruct.SEPARATOR, File.separator), children);
    }
    
    protected DifferenceNode readDifference(int offset, boolean child) throws IOException {
        return readDifference(offset, child, null);
    }
    
    protected DifferenceNode readDifference(int offset, DiffParentNode parent) throws IOException {
        return readDifference(offset, false, parent);
    }
    
    private DifferenceNode readDifference(int offset, boolean child, Node parent) throws IOException {
        EntryStruct entry = readEntry(offset, child);
        if (parent == null) {
            return new DiffChildNode(this, entry, offset);   
        }
        return new DiffChildNode(this, entry, offset, parent);
    }
    
    protected EntryStruct readEntry(int offset, boolean child) throws IOException {
        in.seek(offset);
        return EntryStruct.create(in, child);
    }

    @Override
    public void close() {
        if (in == null) return;
        
        try {
            in.close();
        } catch (IOException ioe) {
            log.error("Error closing input during shutdown.", ioe);
        }
    }

    @Override
    public void useFilter(Filter<Node> filter) {
        //this operation is not supported - we don't
        //want to filter the diffs
    }

    @Override
    public Object getProperty(String name) {
        if (HashProperties.ALGORITHM.equals(name)) {
            return hash.name();
        } else if (TimeProperties.TIMESTAMP.equals(name)) {
            return timestamp();
        }
        
        return null;
    }

    @Override
    public Set<String> properties() {
        return properties;
    }

    @Override
    public int compareTo(DifferenceInventory other) {
        return Long.valueOf(timestamp()).compareTo((Long) other.getProperty(TimeProperties.TIMESTAMP));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DifferenceInventory) {
            return compareTo((DifferenceInventory)obj) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(lkg).hashCode();
    }

    private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        initialize();
    }
    
    private void writeObject(ObjectOutputStream stream)
    throws IOException {
        stream.defaultWriteObject();
    }
}
