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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.index.HeaderStruct;
import com.solers.delivery.inventory.node.FilteredNode;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.Filter;

/**
 * The InventoryReader takes an inventory file as created by the InventoryWriter
 * and turns its contents into a Java-compatible object graph.
 */
public final class InventoryReader implements Inventory, Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger log = Logger.getLogger(InventoryReader.class);
    private static final byte version = 1;
    private transient RandomAccessFile in;
    private transient EntryStruct root;
    private File sourceFile;
    private int rootOffset;
    private Filter<Node> filter = null;
    
    /**
     * Read the specified file.
     * @param index File containing data
     * @throws IOException
     */
    public InventoryReader(File index) throws IOException {
        if (index == null)
            throw new IllegalArgumentException("Index file cannot be null.");
        if (!index.exists())
            throw new IllegalArgumentException("The index file specified does not exist.");
        if (!index.canRead())
            throw new IllegalArgumentException("The index file must be readable.");
        
        sourceFile = index;
        initialize();
    }
    
    protected synchronized EntryStruct readEntry(int offset) throws IOException {
        in.seek(offset);
        return EntryStruct.create(in);
    }
    
    private void initialize() throws IOException {
        initializeInput();
        rootOffset = readHeader();
        initializeRoot();
    }
    
    private int readHeader() throws IOException {
        HeaderStruct header = HeaderStruct.create(in);
        if (header == null) {
            close();
            throw new IOException("The index file cannot be read.");
        }
        if (header.version != version) {
            close();
            throw new IOException("The index file version does not match. (expected " + version + ")");
        }
        return header.rootOffset;
    }
    
    private void initializeRoot() throws IOException {
        this.root = readEntry(rootOffset);
    }
    
    private void initializeInput() throws IOException {
        in = new RandomAccessFile(sourceFile, "r");
    }
    
    /**
     * Get the root node of the inventory.
     * @return the root node
     */
    @Override
    public Node root() {
        return FilteredNode.applyFilter(new IndexedNode(this, root, rootOffset), filter);
    }
    
    @Override
    public String getRootName() {
        return root.name;
    }
    
    @Override
    public void useFilter(Filter<Node> filter) {
        this.filter = filter;
    }
       
    /**
     * Find an arbitrary node in the inventory.
     * @param path the path of the node inside this inventory
     * @return the node
     * @throws FileNotFoundException if the specified path cannot be found inside the inventory
     */
    @Override
    public Node read(String path) {
        String normalizedPath = FilenameUtils.separatorsToUnix(path);
        String[] pathTokens = normalizedPath.split("/");
        String rootName = pathTokens[0];
        if (!root.name.equals(rootName)) return null;
        Node context = root();
        for (int i = 1; i < pathTokens.length; ++i) {
            context = findChild(context, pathTokens[i]);
            if (context == null) break;
        }
        return context != null ? FilteredNode.applyFilter(context, filter) : null;
    }
    
    protected Node findChild(Node parent, String childName) {
        if (parent == null) return null;
        if (!parent.isDirectory()) return null;
        for (Node f : parent.getChildren()) {
            if (f.getName().equals(childName)) return f;
        }
        return null;
    }
    
    protected void finalize() {
        close();
    }
    
    @Override
    public void close() {
        if (in == null) {
            return;
        }
        
        try {
            in.close();
        } catch (IOException ignore) {
            log.error("Error closing input during shutdown", ignore);
        }
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

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> properties() {
        return Collections.EMPTY_SET;
    }
}
