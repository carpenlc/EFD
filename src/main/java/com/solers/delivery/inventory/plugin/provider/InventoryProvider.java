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
package com.solers.delivery.inventory.plugin.provider;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.solers.delivery.inventory.Inventory;

/**
 * This interface is the integration mechanism for inventory plugins.
 * The system will use this interface to discover information about
 * the plugin, as well as construct instances of the inventory
 * implementation that it represents.
 * 
 * @author gvanore
 */
public interface InventoryProvider {
    /**
     * Test whether a given file is compatible with this inventory system.
     * @param resource the resource containing inventory data
     * @return true if compatible, false otherwise
     */
    boolean accept(URI resource);
    
    /**
     * @return A bean containing metadata about this inventory plugin.
     */
    ProviderInfo getProviderInfo();
    
    /**
     * @return A collection of beans describing the parameters this inventory accepts.
     */
    Collection<Parameter> getParameterInfo();
    
    /**
     * Generate an EFD Inventory object based on the given file.  This
     * method will only be invoked on File objects that have returned a
     * true value from the accept method. (But you should still be careful.)
     *
     * @param resource The resource containing inventory data.
     * @param parameters A map of parameter names to objects.
     * @return An EFD-compatible Inventory abstraction
     */
    Inventory newInstance(URI resource, Map<String, Object> parameters);
}
