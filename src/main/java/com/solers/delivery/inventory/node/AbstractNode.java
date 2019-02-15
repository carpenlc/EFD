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

import org.apache.log4j.Logger;

public abstract class AbstractNode implements Node {
    protected static final int DEFAULT_ACCURACY = 1000;
    protected static final Logger log = Logger.getLogger(AbstractNode.class);
    
    private final long size;
    
    public AbstractNode() {
        size = 0;
    }
    
    /**
     * Return the timestamp accuracy of this node, in milliseconds. The largest accuracy 
     * will be used for the sake of comparing timestamp values.  (For example, if node A 
     * has an accuracy of 1000ms and node B has an accuracy of 2000ms, the timestamps will
     * be compared by dividing by 2000ms.
     * @return the accuracy of the timestamp for this node, in milliseconds.
     */
    protected int getTimestampAccuracy() {
        return DEFAULT_ACCURACY;
    }
    
    /**
     * Find what value to use for timestamp accuracy.
     * @param other the other node
     * @return the least accurate node value
     */
    int getTimestampAccuracy(Node other) {
        return Math.max(this.getTimestampAccuracy(), (other instanceof AbstractNode) 
            ? ((AbstractNode)other).getTimestampAccuracy() : DEFAULT_ACCURACY);
    }
    
    protected AbstractNode(long size) {
        this.size = size;
    }
    
    /**
     * Only override this method if you wish to explicitly return a size
     * for a directory.
     */
    @Override
    public long getSize() {
        return this.isDirectory() ? 0 : nodeSize();
    }
    
    /**
     * Override this method if you cannot provide the node size at construction time.
     * @return the size of the node.
     */
    protected long nodeSize() {
        return this.size;
    }
    
    @Override
    public int hashCode() {
        return getPath().hashCode() | (int) getTimestamp() | (int) getSize() | (isDirectory() ? 1 : 0);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Node) {
            Node n = (Node) o;
            if (this.getPath().equals(n.getPath())) {
                if (this.isDirectory() && n.isDirectory()) {
                    //do not compare directory attributes
                    return true;
                } else if (this.isDirectory() ^ n.isDirectory()) {
                    //attributes are implicitly not equal for file/directory comparisons
                    if (log.isDebugEnabled()) log.debug(this.getPath() + " file/directory bits misaligned " + this.isDirectory() + ", " + n.isDirectory());
                    return false;
                }
                //both are files, so use attributes to check
                return sizeEquals(n.getSize()) && timestampEquals(n.getTimestamp(), getTimestampAccuracy(n));
            }
            if (log.isDebugEnabled()) log.debug(this.getPath() + " paths not equal " + n.getPath());
            return false;
        }
        if (log.isDebugEnabled()) log.debug(this.getPath() + " other object not a node " + o.getClass());
        return false;
    }
    
    @Override
    public String toString() {
        return getPath();
    }
    
    @Override
    public int compareTo(Node other) {
        return this.getName().compareTo(other.getName());
    }
    
    @Override
    public Node getChild(String name) {
        for (Node c : getChildren()) {
            if (c.getName().equals(name)) return c;
        }
        return null;
    }
    
    protected boolean timestampEquals(long other, int accuracy) {
        boolean result = (this.getTimestamp() / accuracy) == (other / accuracy); 
        if (!result && log.isDebugEnabled()) {
            log.debug(this.getPath() + " timestamps not equal " + this.getTimestamp() + ", " + other + " (precision " + accuracy + ")");
        }
        return result;
    }
    
    protected boolean sizeEquals(long other) {
        boolean result = this.getSize() == other;
        if (!result && log.isDebugEnabled()) {
            log.debug(this.getPath() + " size not equal " + this.getSize() + ", " + other);
        }
        return result;
    }
}
