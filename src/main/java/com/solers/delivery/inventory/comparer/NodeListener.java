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
package com.solers.delivery.inventory.comparer;

import com.solers.delivery.inventory.node.Node;

/**
 * EventHandler defines the methods necessary for a class to receieve events from the NodeComparer.  An implementation
 * can be created to use the fired events to trigger some type of processing.
 *
 * @author JGimourginas
 */
public interface NodeListener {

    /**
     * Processes an Add event given the node to add
     * @param node Node to add
     */
    void onAdd(Node node);

    /**
     * Processes an Update event given the node with which to perform the update
     * @param node Node with which to update
     */
    void onUpdate(Node node);

    /**
     * Processes a Remove event given the node to remove
     * @param node Node to remove
     */
    void onRemove(Node node);

    /**
     * Processes a Start event
     */
    void onStart();

    /**
     * Processes a Stop event
     */
    void onStop();

}
