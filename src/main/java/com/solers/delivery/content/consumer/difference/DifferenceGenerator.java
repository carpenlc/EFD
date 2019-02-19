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

import com.solers.delivery.content.status.ConsumerProgress;
import mil.nga.efd.domain.ConsumerContentSet;
import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.InventoryFactory;
import com.solers.delivery.inventory.comparer.Comparer;
import com.solers.delivery.inventory.comparer.NodeListener;
import com.solers.delivery.inventory.comparer.UnorderedInventoryComparer;
import com.solers.delivery.inventory.plugin.PluginException;

/**
 * Controls the comparison process between an inventory node and a consumer
 * content set. The generateDifferences method registers a DifferenceRecorder to
 * capture those differences and store them appropriately for later action.
 * 
 * @author JGimourginas
 * @author DBailey
 */
public class DifferenceGenerator implements Runnable {

    private final Comparer comparer;
    private final Comparer prescanComparer;
    private final ConsumerContentSet contentSet;
    private final NodeListener listener;

    public DifferenceGenerator(ConsumerContentSet contentSet, NodeListener listener) {
        this.comparer = new UnorderedInventoryComparer();
        this.prescanComparer = new UnorderedInventoryComparer(true, true);
        this.contentSet = contentSet;
        this.listener = listener;
    }

    /**
     * Finds the differences between an inventory and the content set on the
     * file system.
     * 
     * The differences are handled by the EventHandler passed to
     * <code>initialize</code>.
     * 
     * If a RuntimeException is encountered during the comparison, we MUST call
     * the onStop() callback of the Event Handler. This ensures that any
     * necessary cleanup is performed.
     * 
     * Relies on nodeComparer handling Thread.interrupt() as it's cancellation
     * policy
     * 
     */
    public void run() {
        try {
            runCompare(listener, false);
        } catch (RuntimeException ex) {
            listener.onStop();
        }
    }

    /**
     * Prescan the nodes to get a count of the adds, updates and deletes
     * necessary for the operation
     */
    public void prescan(ConsumerProgress progress) {
        DifferenceCounter counter = new DifferenceCounter();
        runCompare(counter, true);
        progress.initialize(counter.getItemsAdded(),
                            counter.getAddBytes(), 
                            counter.getItemsUpdated(),
                            counter.getUpdateBytes(), 
                            counter.getItemsDeleted(),
                            counter.getDeleteBytes());
        progress.setTotalNewFiles(counter.getFilesAdded() + counter.getFilesUpdated());
    }

    /**
     * Run the comparer with <code>handler</code>
     * 
     * @param handler
     */
    protected void runCompare(NodeListener handler, boolean prescan) {
        Inventory desired = null; 
        Inventory existing = null;
        try {
            desired = InventoryFactory.getInventory(contentSet);
            existing = InventoryFactory.getBackingInventory(contentSet, desired.getRootName());
            (prescan ? prescanComparer : comparer).compare(desired, existing, handler);
        } catch (PluginException pe) {
            throw new RuntimeException("Error loading virtual manifest for content set " + contentSet.getName(), pe);
        } finally {
            if (desired != null) desired.close();
            if (existing != null) existing.close();
        }
    }
}
