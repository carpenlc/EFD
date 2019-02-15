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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Configurable;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.SoftCache;

@Configurable("unorderedInventoryComparer")
public class UnorderedInventoryComparer implements Comparer {

    private NodeListener handler;
    private Inventory desired;
    private Inventory existing;
    
    protected final boolean recurseAdds;
    protected final boolean recurseRemoves;
    protected int heapSize = 1000;
    
    /**
     * TODO: document
     */
    private static final int DONE = 0;
    private static final int EXISTING_EMPTY = 1;
    private static final int DESIRED_EMPTY = 2;
    private static final int NORMAL = 3;
    
    protected static final Iterator<? extends Node> EMPTY_ITERATOR = new EmptyIterator<Node>();
    
    protected static class EmptyIterator<K> implements Iterator<K> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public K next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };
    
    public UnorderedInventoryComparer() {
        this(true, false);
    }
            
    public UnorderedInventoryComparer(boolean recurseAdds, boolean recurseRemoves) {
        this.recurseAdds = recurseAdds;
        this.recurseRemoves = recurseRemoves;
    }
    
    public void setHeapSize(int heapSize) {
        this.heapSize = heapSize;
    }
    
    public synchronized void compare(final Inventory desired, final Inventory existing, final NodeListener theHandler) {
        this.handler = theHandler;
        this.desired = desired;
        this.existing = existing;
        
        try {
            handler.onStart();
            Node droot = desired != null ? desired.root() : null;
            Node eroot = existing != null ? existing.root() : null;
            if (eroot == null && droot == null) {
                return;
            }
            if (eroot == null ? (droot == null ? false : true) : (droot == null) ? true : !eroot.getName().equals(droot.getName())) {
                //if existing and desired root nodes do not match, add/remove them respectively
                compareNodes(droot, null);
                compareNodes(null, eroot);
            } else {
                //do a standard recursion for inventories that have matching root nodes
                recurseCompare(droot, eroot);
            }
        } finally {
            handler.onStop();
            this.handler = null;
            this.desired = null;
            this.existing = null;
        }        
    }
    
    private void recurseCompare(Node desiredNode, Node existingNode) {
        if (Thread.currentThread().isInterrupted()) return;
        
        Iterator<? extends Node> dIter = desiredNode != null ? desiredNode.getChildren().iterator() : EMPTY_ITERATOR;
        Iterator<? extends Node> eIter = existingNode != null ? existingNode.getChildren().iterator() : EMPTY_ITERATOR;
        
        SoftCache<String, Node> dHeap = new SoftCache<String, Node>(heapSize, new InventoryReconstructor(desired));
        SoftCache<String, Node> eHeap = new SoftCache<String, Node>(heapSize, new InventoryReconstructor(existing));
        
        int state = NORMAL;
        
        mainloop: while (dIter.hasNext() || eIter.hasNext()) {
            if (Thread.currentThread().isInterrupted()) break;
            
            Node dNext;
            Node eNext;
            try {
                dNext = dIter.next();
            } catch (NoSuchElementException nsee) {
                dNext = null;
            }
            
            try {
                eNext = eIter.next();
            } catch (NoSuchElementException nsee) {
                eNext = null;
            }
            
            // State transition statement
            switch (state) {
                case NORMAL:
                    if (dNext == null && eNext == null) {
                        state = DONE;
                    } else if (dNext == null && eNext != null) {
                        // desired is empty when both its heap and iterator are clear
                        if (dHeap.isEmpty()) state = DESIRED_EMPTY;
                        else {
                            addExistingHeap(eNext, dHeap, eHeap);
                            continue mainloop;
                        }
                    } else if (dNext != null && eNext == null) {
                        // existing is empty when both its heap and iterator are clear
                        if (eHeap.isEmpty()) state = EXISTING_EMPTY;
                        else {
                            addDesiredHeap(dNext, dHeap, eHeap);
                            continue mainloop;
                        }
                    }
                    break;
                case DONE:
                default:
                    break;
            }
            
            // Node action
            switch (state) {
                case NORMAL:
                    //bypass heaps if nodes are the same path
                    if (dNext.getPath().equals(eNext.getPath())) {
                        compareNodes(dNext, eNext);
                    } else {
                        addDesiredHeap(dNext, dHeap, eHeap);
                        addExistingHeap(eNext, dHeap, eHeap);
                    }
                    break;
                //do not add to the heap when one tree is exhausted
                case DESIRED_EMPTY:
                    compareNodes(checkDesiredHeap(eNext, dHeap), eNext);
                    break;
                case EXISTING_EMPTY:
                    compareNodes(dNext, checkExistingHeap(dNext, eHeap));
                    break;
                case DONE:
                default:
                    break mainloop;
            }
        }
        
        Iterator<Entry<String, Node>> iter;

        //add remaining desired
        iter = dHeap.iterator();
        while (iter.hasNext()) {
            if (Thread.currentThread().isInterrupted()) break;
            Node n = iter.next().getValue();
            compareNodes(n, checkExistingHeap(n, eHeap));
            iter.remove();
        }
        
        //remove remaining existing
        iter = eHeap.iterator();
        while (iter.hasNext()) {
            if (Thread.currentThread().isInterrupted()) break;
            Node n = iter.next().getValue();
            compareNodes(checkDesiredHeap(n, dHeap), n);
            iter.remove();
        }
    }
    
    private void addExistingHeap(Node e, SoftCache<String, Node> dHeap, SoftCache<String, Node> eHeap) {
        Node d = checkDesiredHeap(e, dHeap);
        if (d == null) {
            eHeap.put(e.getPath(), e);
        } else {
            compareNodes(d, e);
        }
    }
    
    private void addDesiredHeap(Node d, SoftCache<String, Node> dHeap, SoftCache<String, Node> eHeap) {
        Node e = checkExistingHeap(d, eHeap);
        if (e == null) {
            dHeap.put(d.getPath(), d);
        } else {
            compareNodes(d, e);
        }
    }
    
    private Node checkExistingHeap(Node d, SoftCache<String, Node> eHeap) {
        return eHeap.remove(d.getPath());
    }
    
    private Node checkDesiredHeap(Node e, SoftCache<String, Node> dHeap) {
        return dHeap.remove(e.getPath());
    }
    
    private void compareNodes(Node d, Node e) {
        if (e == null && d == null) {
            return;
        } else if (e == null && d != null) {
            add(d);
        } else if (e != null && d == null) {
            remove(e);
        } else {
            if (!d.equals(e)) {
                update(d);
            } else {
                if (d.isDirectory()) {
                    recurseCompare(d, e);
                }
            }
        }
    }
    
    private void add(Node n) {
        handler.onAdd(n);
        if (recurseAdds && n.isDirectory()) {
            recurseCompare(n, null);
        }
    }
    
    private void remove(Node n) {
        handler.onRemove(n);
        if (recurseRemoves && n.isDirectory()) {
            recurseCompare(null, n);
        }
    }
    
    private void update(Node n) {
        handler.onUpdate(n);
        if (recurseAdds && n.isDirectory()) {
            //n was likely changed from a file to a directory
            recurseCompare(n, null);
        }
    }
    
    protected static class InventoryReconstructor implements SoftCache.Reconstructor<String, Node> {
        private final Inventory source;
        
        public InventoryReconstructor(Inventory source) {
            this.source = source;
        }
        
        @Override
        public Node rebuild(String key) {
            return source.read(key);
        }
    }
}
