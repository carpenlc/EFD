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
package com.solers.delivery.inventory.node;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The Node class is used to define constants that can be used by all Node implementations.  All
 * methods defined by the Node class are abstracted, so any class that extends Node must implement those
 * methods.
 * @author JGimourginas
 */
public interface Node extends Comparable<Node> {
    /**
     * retrieves node name
     * @return name of node
     */
    String getName();

    /**
     * retrieves children as List of Node objects
     * @return List of Nodes that are children to this node - only possible if isAbleToHaveChildren() is true,
     * null otherwise
     */
    List<Node> getChildren();

    /**
     * retrieves node size
     * @return size of node in bytes
     */
    long getSize();

    /**
     * retrieves timestamp of node
     * @return timestamp in millis since epoch
     */
    long getTimestamp();

    /**
     * retrieves path to this node starting at the root of the node structure - e.g. "root/dir1/file1" or "root/file1"
     * or "root".  There will be NO leading '/' or '\\', the path will start with the root node's name.
     * @return path to node relative to root of node structure
     */
    String getPath();

    /**
     * true if node can have children, false otherwise - true DOES NOT indicate the node currently has children, but
     * rather that the node could potentially have children
     * @return true if node has potential to be parent, false otherwise
     */
    boolean isDirectory();
    
    /**
     * Return the parent of this node, or null if there is no parent (in the case of the root
     * node).
     * @return parent or null
     */
    Node getParent();
    
    /**
     * Return the child with the given name, or null if it does not exist.
     * @return child or null
     */
    Node getChild(String name);
    
    /**
     * Open a stream to the underlying representation, or null if this is 
     * not possible.  If an InputStream is returned, the caller must close
     * it when finished reading.
     */
    InputStream openStream() throws IOException;
}
