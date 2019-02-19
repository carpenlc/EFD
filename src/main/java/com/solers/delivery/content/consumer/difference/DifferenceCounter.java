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
package com.solers.delivery.content.consumer.difference;

import com.solers.delivery.inventory.comparer.NodeListener;
import com.solers.delivery.inventory.node.Node;

/**
 * EventHandler to count the number of each operation that is performed
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class DifferenceCounter implements NodeListener {

    /**
     * Number of adds
     */
    private long itemsAdded;
    private long filesAdded;

    /**
     * Number of updates
     */
    private long itemsUpdated;
    private long filesUpdated;

    /**
     * Number of deletes
     */
    private long itemsDeleted;

    /**
     * Add bytes
     */
    private long addBytes;

    /**
     * Update bytes
     */
    private long updateBytes;

    /**
     * Delete bytes
     */
    private long deleteBytes;

    /**
     * Default constructor
     */
    public DifferenceCounter() {
        itemsAdded = 0;
        itemsUpdated = 0;
        itemsDeleted = 0;
        addBytes = 0;
        updateBytes = 0;
        deleteBytes = 0;
    }

    /**
     * @see com.solers.delivery.inventory.comparer.NodeListener#onAdd(com.solers.delivery.inventory.node.Node)
     */
    @Override
    public void onAdd(Node node) {
        itemsAdded++;
        if (!node.isDirectory()) {
            filesAdded++;
        }
        
        addBytes += node.getSize();
    }

    /**
     * @see com.solers.delivery.inventory.comparer.NodeListener#onUpdate(com.solers.delivery.inventory.node.Node)
     */
    @Override
    public void onUpdate(Node node) {
        itemsUpdated++;
        if (!node.isDirectory()) {
            filesUpdated++;
        }
        updateBytes += node.getSize();
    }

    /**
     * @see com.solers.delivery.inventory.comparer.NodeListener#onRemove(com.solers.delivery.inventory.node.Node)
     */
    @Override
    public void onRemove(Node node) {
        itemsDeleted++;
        deleteBytes += node.getSize();
    }

    /**
     * @see com.solers.delivery.inventory.comparer.NodeListener#onStart()
     */
    @Override
    public void onStart() {
    }

    /**
     * @see com.solers.delivery.inventory.comparer.NodeListener#onStop()
     */
    @Override
    public void onStop() {
    }
    
    /**
     * @return the number of bytes that will be added
     */
    public long getAddBytes() {
        return addBytes;
    }

    /**
     * @return the number of bytes that will be updated
     */
    public long getUpdateBytes() {
        return updateBytes;
    }

    /**
     * @return the number of bytes that will be removed
     */
    public long getDeleteBytes() {
        return deleteBytes;
    }

    public long getItemsAdded() {
        return itemsAdded;
    }

    public long getItemsUpdated() {
        return itemsUpdated;
    }

    public long getItemsDeleted() {
        return itemsDeleted;
    }

    public long getFilesAdded() {
        return filesAdded;
    }

    public long getFilesUpdated() {
        return filesUpdated;
    }

}
