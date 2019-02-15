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
package com.solers.delivery.inventory;

import java.util.Set;

import com.solers.delivery.inventory.node.Node;
import com.solers.util.Filter;

/**
 * <p>This is the super interface for any inventory system that we
 * may choose to couple to the EFD.</p>
 * 
 * <p>After reading from an inventory, you must call close()</p>
 */
public interface Inventory {
    /**
     * Retrieve the root node of the inventory.
     */
    Node root();
    
    /**
     * Find the name of the root node. This may be useful when the root node
     * itself is filtered from the tree.  This method should NOT be a shortcut
     * to root().getName() - the point of this is to provide the name when the
     * root node itself is excluded by a filter.
     * @return name of root node, even if the root is filtered
     */
    String getRootName();
    
    /**
     * Retrieve an arbitrary node from the inventory.
     * @param path The path identifier for the node.
     * @return The node, or null, if it is not in the inventory.
     */
    Node read(String path);
    
    /**
     * Use the supplied filter when retrieving nodes
     */
    void useFilter(Filter<Node> filter);
    
    /**
     * Close the inventory and/or free any resources
     */
    void close();
    
    /**
     * Retrieve a property of this inventory.
     * @param name the name of the property
     * @return the property
     */
    Object getProperty(String name);
    
    /**
     * The list of extra properties that this inventory supports.
     * @return set of property names
     */
    Set<String> properties();
}
