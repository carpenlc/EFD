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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.solers.delivery.inventory.DifferenceInventory;
import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;

/**
 * The MergedNode applies the differences from a difference
 * file to an inventory as controlling code walks the tree.
 */
public class MergedNode extends AbstractNode {
    private final Node inventoryNode;
    /**
     * Using a sorted set is important, because we want to apply the differences in the order
     * in which they were collected.  The DifferenceFileReader implements Comparable<DifferenceInventory>
     * and uses the time stamp as the means of ordering.
     */
    protected final SortedSet<DifferenceInventory> differences = new TreeSet<DifferenceInventory>();
    private final List<Node> appliedDifferences;
    
    /**
     * Transform the inventory starting at inventoryNode with the supplied
     * set of differences.
     * @param inventoryNode The inventory to transform
     * @param differenceInventory The difference inventory to use.
     */
    public MergedNode(Node inventoryNode, DifferenceInventory differenceInventory) {
        this(inventoryNode, Arrays.asList(new DifferenceInventory[] { differenceInventory }));
    }
    
    /**
     * Using the differences in the specified collection of  difference inventories, transform
     * the inventory as it is walked by controlling code.  The collection is sorted such that
     * older differences are applied before newer ones.
     * @param inventoryNode The inventory node to transform.
     * @param differenceCollection The collection of differences to use to transform
     */
    public MergedNode(Node inventoryNode, Collection<DifferenceInventory> differenceCollection) {
        super(inventoryNode.getSize());
        this.inventoryNode = inventoryNode;
        for (DifferenceInventory inv : differenceCollection) {
            differences.add(inv);
        }
        this.appliedDifferences = applyDifferences();
    }
    
    /**
     * Apply all differences to this node, successively, until the most recent
     * set of differences has been applied.
     * @return
     */
    private List<Node> applyDifferences() {
        List<Node> children = inventoryNode.getChildren();
        for (DifferenceInventory differenceInventory : differences) {
            children = applyDifferences(children, differenceInventory);
        }
        return children;
    }
    
    /**
     * Apply the differences of this node to the inventory node in
     * question.
     * @return a list of children with differences applied.
     */
    private List<Node> applyDifferences(List<Node> inventoryChildren, DifferenceInventory differenceInventory) {
        Node diffParent = differenceInventory.read(inventoryNode.getPath());

        // There are no differences recorded for this node, so we will
        // use the children provided by the inventory.
        if (diffParent == null) {
            return inventoryChildren;
        }
        
        // We construct a list of of merged nodes by
        // 1.) creating a set of all differences
        // 2.) subtracting existing node <-> diff node matches from the diff set
        // 3.) Removing or changing the existing nodes based on diff
        // 4.) adding remaining diffs (the additions to the inventory) to the children
        List<Node> children = new ArrayList<Node>();
        Set<Node> diffNodes = new HashSet<Node>(diffParent.getChildren());
        
        // Process updates and removes from the existing nodes
        for (Node n : inventoryChildren) {
            DifferenceNode dn = (DifferenceNode) diffParent.getChild(n.getName());
            if (dn != null) {
                diffNodes.remove(dn);
                //implicitly drop removed nodes
                if (dn.isChanged()) {
                    children.add(dn);
                }
            } else {
                children.add(n);
            }
        }
        
        // Process any remaining added nodes from the difference set
        for (Node n : diffNodes) {
            DifferenceNode dn = (DifferenceNode) n;
            if (dn.isRemoved()) continue; //make sure node is not removed, just in case
            children.add(dn);
        }
        
        return children;
    }
    
    @Override
    public List<Node> getChildren() {
      // We wrap the differences with this simple inner class that ensures
      // that the merge process continues down the tree, without needing
      // to iterate and wrap again or make sure that a new MergedNode is
      // constructed at all possible points in applyDifferences().
      return new ArrayList<Node>(appliedDifferences) {
          private static final long serialVersionUID = 1L;

          @Override
          public Node get(int index) {
              Node n = super.get(index);
              return new MergedNode(n, differences);
          }
      };
    }

    @Override
    public String getName() {
        return inventoryNode.getName();
    }

    @Override
    public Node getParent() {
        return inventoryNode.getParent();
    }

    @Override
    public String getPath() {
        return inventoryNode.getPath();
    }

    @Override
    public long getTimestamp() {
        return inventoryNode.getTimestamp();
    }

    @Override
    public boolean isDirectory() {
        return inventoryNode.isDirectory();
    }
    
    
    @Override
    public InputStream openStream() throws IOException {
        return inventoryNode.openStream();
    }
}
