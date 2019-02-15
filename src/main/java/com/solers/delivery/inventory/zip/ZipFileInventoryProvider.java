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
package com.solers.delivery.inventory.zip;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipFile;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.plugin.PluginException;
import com.solers.delivery.inventory.plugin.provider.InventoryParameter;
import com.solers.delivery.inventory.plugin.provider.InventoryProvider;
import com.solers.delivery.inventory.plugin.provider.Parameter;
import com.solers.delivery.inventory.plugin.provider.ProviderInfo;
import com.solers.delivery.inventory.plugin.provider.StandardProviderInfo;

public class ZipFileInventoryProvider implements InventoryProvider {
    private static final InventoryParameter rootName = new InventoryParameter(
        "root", String.class, false, false, 
        "The name of the root node for this inventory (if unspecified, will use file name minus extension).");
    
    private static final Collection<Parameter> parameterInfo = Collections.unmodifiableCollection(
        new HashSet<Parameter>(Arrays.asList(rootName)));
            
    private static final ProviderInfo providerInfo = new StandardProviderInfo(
        "NGA", "Zip Archive Inventory", "2.2");
    
    @Override
    public boolean accept(URI resource) {
        if (resource != null) {
            String scheme = resource.getScheme();
            scheme = scheme == null ? "" : scheme;
            if (scheme.equals("file")) {
                File zipFile = new File(resource);
                try {
                    ZipFile zf = new ZipFile(zipFile, ZipFile.OPEN_READ);
                    zf.close();
                    return true;
                } catch (IOException ioe) {
                }
            }
        }
        return false;
    }

    @Override
    public Collection<Parameter> getParameterInfo() {
        return parameterInfo;
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return providerInfo;
    }

    @Override
    public Inventory newInstance(URI resource, Map<String, Object> parameters) {
        try {
            if (!accept(resource)) return null;
            String root = (String) rootName.verifyAndRetrieve(parameters);
            return new ZipFileInventory(new File(resource), root);
        } catch (PluginException pe) {
            throw new RuntimeException(pe);
        }
        
    }

}
