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

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.solers.delivery.inventory.comparer.NodeListener;
import com.solers.delivery.inventory.node.Node;

/**
 * Puts ContentDifference into Queues for processing
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class DifferenceQueuer implements NodeListener {

    private static final Logger log = Logger.getLogger(DifferenceQueuer.class);
    
    private final BlockingQueue<ContentDifference> refreshes;
    private final BlockingQueue<ContentDifference> deletes;
    private final Long contentSetId;
    private final Integer requesters;

    /**
     * Constructor
     * 
     * @param refreshes
     * @param deletes
     * @param contentSetId
     */
    public DifferenceQueuer(BlockingQueue<ContentDifference> refreshes, BlockingQueue<ContentDifference> deletes, Long contentSetId, Integer requesters) {
        this.refreshes = refreshes;
        this.deletes = deletes;
        this.contentSetId = contentSetId;
        this.requesters = requesters;
    }

    /**
     * @see com.solers.delivery.inventory.comparer.NodeListener#onAdd(com.solers.delivery.inventory.node.Node)
     */
    @Override
    public void onAdd(Node node) {
        put(refreshes, node, ContentDifferenceActions.ADD);
    }

    /**
     * @see com.solers.delivery.inventory.comparer.NodeListener#onUpdate(com.solers.delivery.inventory.node.Node)
     */
    @Override
    public void onUpdate(Node node) {
        put(refreshes, node, ContentDifferenceActions.REFRESH);
    }

    /**
     * @see com.solers.delivery.inventory.comparer.NodeListener#onRemove(com.solers.delivery.inventory.node.Node)
     */
    @Override
    public void onRemove(Node node) {
        put(deletes, node, ContentDifferenceActions.REMOVE);
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
        put(deletes, null, ContentDifferenceActions.DONE);
        for (int i=0; i < requesters; i++) {
            put(refreshes, null, ContentDifferenceActions.DONE);
        }
    }

    /**
     * Put a ContentDifference onto <code>queue</code>
     * 
     * @param queue
     * @param node
     * @param action
     */
    private void put(BlockingQueue<ContentDifference> queue, Node node, ContentDifferenceActions action) {
        ContentDifference difference;
        if (node == null) {
            difference = new ContentDifference(action);
        } else {
            difference = new ContentDifference(node, action, contentSetId);
        }
        try {
            queue.put(difference);
        } catch (InterruptedException ex) {
            log.info("Interrupted");
            Thread.currentThread().interrupt();
        }
    }
}
