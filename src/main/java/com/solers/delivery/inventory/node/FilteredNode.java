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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.solers.util.Filter;

/**
 * The FilteredNode takes a Filter<Node> object to mask children. It wraps all
 * children in FilteredNode adapters.
 */
public class FilteredNode extends AbstractNode implements Serializable {

    private static final long serialVersionUID = 1l;

    private final Node node;
    protected final Filter<Node> filter;

    /**
     * Create a FilteredNode.
     * 
     * @param node
     *            The backing node to adapt.
     * @param filter
     *            The filter that determines which children shall be included in
     *            the getChildren() call.
     */
    protected FilteredNode(Node node, Filter<Node> filter) {
        Node n = node;
        if (node instanceof FilteredNode) {
            n = ((FilteredNode) node).node; 
        }
        this.node = n;
        this.filter = filter;
    }

    public String getName() {
        return node.getName();
    }

    public List<Node> getChildren() {
        final List<? extends Node> children = node.getChildren();
        /*
         * This inner class overrides the iterator so that
         * we can skip iterating and copying the backing
         * node's children and simply iterate the filtered
         * nodes.
         */
        return new ArrayList<Node>() {
            private static final long serialVersionUID = 1L;
            
            /**
             * The get/size/etc methods of this list use the unfiltered
             * list as a backing - therefore we must apply the filter
             * now.
             * @return null if the object at the specified index does not pass the filter
             */
            @Override
            public Node get(int index) {
                Node n = children.get(index);
                if (filter.accept(n)) {
                    return new FilteredNode(n, filter);
                }
                return null;
            }
            
            @Override
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {
                    int cursor = 0;
                    private Node next = null;
                    @Override
                    public boolean hasNext() {
                        if (cursor < children.size()) {
                            next = get(cursor);
                            if (next != null) return true;
                            cursor++;
                            return hasNext();
                        }
                        return false;
                    }

                    @Override
                    public Node next() {
                        if (hasNext()) {
                            cursor++;
                            return next;
                        }
                        throw new NoSuchElementException();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public long getSize() {
        return node.getSize();
    }

    public long getTimestamp() {
        return node.getTimestamp();
    }

    public String getPath() {
        return node.getPath();
    }

    public boolean isDirectory() {
        return node.isDirectory();
    }
    
    public Node getParent() {
        Node parent = node.getParent();
        if (parent == null) return null;
        if (parent instanceof FilteredNode) return parent;
        return new FilteredNode(parent, filter);
    }
    
    @Override
    public Node getChild(String name) {
        Node child = node.getChild(name);
        return applyFilter(child, filter);
    }
    
    @Override
    public InputStream openStream() throws IOException {
        return node.openStream();
    }
    
    public static Node applyFilter(Node n, Filter<Node> filter) {
        if (n == null) return null;
        if (filter == null) return n;
        if (!filter.accept(n)) return null;
        
        //make sure no parents have been excluded by this filter
        Node p = n;
        while ((p = p.getParent()) != null) {
            if (!filter.accept(p)) return null;
        }
        return new FilteredNode(n, filter);
    }
}
