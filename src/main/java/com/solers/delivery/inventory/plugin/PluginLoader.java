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
package com.solers.delivery.inventory.plugin;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.solers.delivery.inventory.plugin.provider.ProviderInfo;

public final class PluginLoader {
    private static final Logger log = Logger.getLogger(PluginLoader.class);
    private PluginLoader() {
        
    }
    
    public static Collection<ProviderInfo> load(String location) {
        File loc = new File(location);
        if (loc.exists() && loc.isDirectory()) {
            return load(loc);
        } else {
            try {
                return load(PluginLoader.class.getClassLoader().getResources(location));
            } catch (Exception e) {
                log.error("Could not find plugin resource: " + location, e);
                return Collections.emptySet();
            }
        }
    }
    
    private static Collection<ProviderInfo> load(Enumeration<URL> resources) throws Exception {
        Set<ProviderInfo> providers = new HashSet<ProviderInfo>();
        while (resources.hasMoreElements()) {
            File loc = new File(resources.nextElement().toURI());
            if (loc.exists() && loc.isDirectory()) {
                providers.addAll(load(loc));
            }
        }
        return providers;
    }
    
    private static Collection<ProviderInfo> load(File location) {
        File[] plugins = location.listFiles();
        if (plugins == null) return Collections.emptySet();
        Set<ProviderInfo> info = new HashSet<ProviderInfo>(plugins.length);
        for (File plugin : plugins) {
            try {
                log.info("Loading inventory plugin: " + plugin.getName());
                ProviderInfo pi = InventoryPlugin.register(plugin);
                log.info("Loaded successfully: " + pi.toString());
                info.add(pi);
            } catch (PluginException pe) {
                log.warn("Error loading plugin " + location.getAbsolutePath(), pe);
            }
        }
        return info;
    }
}
