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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.solers.delivery.inventory.index.HashUtil;
import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;

/**
 * The DiffParentNode is the entry that <i>contains</i> differences.  If
 * the node a/b/c is changed, then a DiffParentNode a/b will live in the
 * difference file and contain c as a child.  This node does not encapsulate
 * an actual difference.
 */
public final class DiffParentNode extends AbstractNode {
    private static final Logger log = Logger.getLogger(DiffParentNode.class);
    
    private final String path;
    private final int[] children;
    private final DifferenceFileReader diffReader;
    
    DiffParentNode(DifferenceFileReader diffReader, String path, int[] children) {
        super(0);
        this.diffReader = diffReader;
        this.path = path;
        this.children = children;
    }
    
    @Override
    public List<Node> getChildren() {
        List<Node> list = new ArrayList<Node>();
        
        for (int i = 0; i < children.length; ++i) {
            try { 
                list.add(diffReader.readDifference(children[i], this));
            } catch (IOException ioe) {
                log.debug("IO exception reading difference file: ", ioe);
            }
        }
        
        return list;
    }
    
    /**
     * Provide random access lookup to child diff nodes.
     * @param name the name of the node
     * @return the difference, if found, or null for none.
     */
    @Override
    public DifferenceNode getChild(String name) {
        int start = HashUtil.index(name, diffReader.getHash(), children.length);
        int location = start;
        try {
            DifferenceNode difference = diffReader.readDifference(children[start], this);
            while (!difference.getName().equals(name)) {
                location++;
                location %= children.length;
                if (location == start) return null;
                difference = diffReader.readDifference(children[location], this);
            }
            return difference;
        } catch (IOException ioe) {
            log.debug("IO exception reading difference file: ", ioe);
            return null;
        }
    }

    @Override
    public String getName() {
        if (path.equals(HeaderStruct.ROOT_PARENT)) return "<ROOT>";
        else {
            if (path.lastIndexOf(File.separator) > -1)
                return path.substring(path.lastIndexOf(File.separator) + 1);
            return path;
        }
    }

    @Override
    public Node getParent() {
        return null;
    }
    
    @Override
    public InputStream openStream() throws IOException {
        return null;
    }

    @Override
    public String getPath() {
        return path.replace(HeaderStruct.SEPARATOR, File.separator);
    }

    @Override
    public long getTimestamp() {
        return 0;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }
}
