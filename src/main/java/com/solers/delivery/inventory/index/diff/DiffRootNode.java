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
import java.util.List;

import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;

public final class DiffRootNode extends AbstractNode {
    private final DifferenceFileReader inventory;
    
    protected DiffRootNode(DifferenceFileReader inventory) {
        super(0);
        this.inventory = inventory;
    }
    
    @Override
    public List<Node> getChildren() {
        return new ArrayList<Node>(inventory.roots());
    }
    
    @Override
    public Node getChild(String name) {
        return inventory.read(name);
    }

    @Override
    public String getName() {
        return HeaderStruct.ROOT_PARENT;
    }
    
    @Override
    public InputStream openStream() throws IOException {
        return null;
    }

    @Override
    public Node getParent() {
        return null;
    }

    @Override
    public String getPath() {
        return HeaderStruct.ROOT_PARENT;
    }

    @Override
    public long getTimestamp() {
        return inventory.timestamp();
    }

    @Override
    public boolean isDirectory() {
        return true;
    }
}
