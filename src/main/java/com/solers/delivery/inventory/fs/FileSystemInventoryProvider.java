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
package com.solers.delivery.inventory.fs;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.plugin.provider.InventoryProvider;
import com.solers.delivery.inventory.plugin.provider.Parameter;
import com.solers.delivery.inventory.plugin.provider.ProviderInfo;
import com.solers.delivery.inventory.plugin.provider.StandardProviderInfo;

public class FileSystemInventoryProvider implements InventoryProvider {
    private static final Logger log = Logger.getLogger(FileSystemInventoryProvider.class);
    private static final ProviderInfo info = new StandardProviderInfo(
        "NGA",
        "Filesystem",
        "2.2"
    );
    
    @Override
    public boolean accept(URI resource) {
        try {
            if (resource != null) {
                String scheme = resource.getScheme();
                scheme = scheme == null ? "" : scheme;
                if (scheme.equals("file")) {
                    File f = new File(resource);
                    if (f.exists()) {
                        return f.isDirectory();
                    }
                    //accept non-existent file locations as empty inventories
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Caught an exception while examining " + resource, e);
        }
        return false;
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return info;
    }
    
    @Override
    public Collection<Parameter> getParameterInfo() {
        return Collections.emptySet();
    }

    @Override
    public Inventory newInstance(URI resource, Map<String, Object> parameters) {
        if (accept(resource)) {
            return new FileSystemInventory(new File(resource));
        }
        return null;
    }
}
