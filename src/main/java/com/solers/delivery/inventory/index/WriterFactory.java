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
package com.solers.delivery.inventory.index;

import java.net.URI;
import java.util.Map;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.Writer;
import com.solers.delivery.inventory.node.Node;
import com.solers.delivery.inventory.plugin.InventoryPlugin;
import com.solers.delivery.inventory.plugin.PluginException;

public final class WriterFactory {
    public static final int DEFAULT_VERSION = 2;
    
    private WriterFactory() {
        
    }
    
    /**
     * Return a new instance based on the root node.
     */
    public static Writer newInstance(Node rootNode) {
        return new com.solers.delivery.inventory.index.v2.InventoryWriter(rootNode);
    }
    
    public static Writer newInstance(Inventory inv) {
        return new com.solers.delivery.inventory.index.v2.InventoryWriter(inv);
    }
    
    public static Writer newInstance(URI resource, Map<String, Object> parameters) {
        Inventory i = null;
        try {
            i = InventoryPlugin.newInstance(resource, parameters);
        } catch (PluginException pe) {
            throw new IllegalArgumentException("Resource could not be loaded: " + resource);
        }
        
        return new com.solers.delivery.inventory.index.v2.InventoryWriter(i);
    }
    
    public static Writer newInstance(Inventory backingInventory, String hashingAlgorithm) {
        return new com.solers.delivery.inventory.index.v2.InventoryWriter(backingInventory, hashingAlgorithm);
    }
    
    public static Writer newInstance(Node rootNode, String hashingAlgorithm) {
        return new com.solers.delivery.inventory.index.v2.InventoryWriter(rootNode, hashingAlgorithm);
    }
  
    public static Writer newInstance(Node rootNode, String hashingAlgorithm, long timestamp) {
        return new com.solers.delivery.inventory.index.v2.InventoryWriter(rootNode, hashingAlgorithm, timestamp);
    }
}
