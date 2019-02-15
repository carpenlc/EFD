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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.plugin.provider.InventoryProvider;
import com.solers.delivery.inventory.plugin.provider.Parameter;
import com.solers.delivery.inventory.plugin.provider.ProviderInfo;

/**
 * <p>This class is the gateway to the inventory plug in system.  With it,
 * client code can:</p>
 * <ol>
 * <li>Register a plugin archive (jar file).</li>
 * <li>Get inventory implementations for a file, if a provider handles it.</li>
 * <li>Inspect registered inventory plugins.</li>
 * </ol>
 * <p>Inventory plugins must be a java-compatible .jar archive.  Requirements:</p>
 * <ul>
 * <li>There must exist a META-INF directory</li>
 * <li>Inside the META-INF directory there must exist a file named 'provider'</li>
 * <li>The provider file contains, on the first line, a fully-qualified Java class name</li>
 * <li>The Java class specified in the provider implements com.solers.delivery.inventory.plugin.provider.InventoryProvider</li>
 * <li>All dependencies of the InventoryProvider and its dependent classes are satisfied within the plugin .jar archive.</li>
 * </ul>
 * <p>Satisfying the above requirements will indirectly require implentations of:</p>
 * <ul>
 * <li>com.solers.delivery.inventory.Inventory</li>
 * <li>com.solers.delivery.inventory.node.Node</li>
 * </ul>
 * <p>The plugin developer should strongly consider extending AbstractNode instead of implementing Node.
 * The abstract class contains necessary implementations of toString, hashCode, and equals.</p>
 *
 * @author gvanore
 */
public final class InventoryPlugin {
    private static final Logger log = Logger.getLogger(InventoryPlugin.class);
    private static final String PLUGIN_DESCRIPTOR_RESOURCE = "META-INF/provider";
    protected static final ConcurrentMap<String, InventoryProvider> registeredPlugins = 
        new ConcurrentHashMap<String, InventoryProvider>(); 
    
    private InventoryPlugin() {
        //static utility class
    }

    protected static URLClassLoader newJarFileClassLoader(File jarFile) throws PluginException {
        return newJarFileClassLoader(jarFile, Thread.currentThread().getContextClassLoader());
    }
    
    protected static URLClassLoader newJarFileClassLoader(final File jarFile, final ClassLoader parent) throws PluginException {
        if (jarFile == null) throw new PluginLoadException("Jar file object cannot be null.", new NullPointerException());
        if (!jarFile.exists()) throw new PluginLoadException("Plugin file does not exist: " + jarFile.getAbsolutePath());
        if (!jarFile.isFile()) throw new PluginLoadException("Plugin file should not be a directory: " + jarFile.getAbsolutePath());
        try {
            JarFile jf = new JarFile(jarFile);
            jf.close();
        } catch (IOException ioe) {
            throw new PluginLoadException("File is not a jar file or is corrupt: " + jarFile.getAbsolutePath());
        }
        
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<URLClassLoader>(){
                @Override
                public URLClassLoader run() throws PluginLoadException {
                    try {
                        return new URLClassLoader(new URL[] {
                            new URL("file", null, jarFile.getAbsolutePath())
                        }, parent);
                    } catch (MalformedURLException mue) {
                        throw new PluginLoadException("Failed to load jar file: " + jarFile.getAbsolutePath(), mue);
                    }
                }
                
            });
        } catch (PrivilegedActionException ex) {
            throw new PluginLoadException(ex.getCause().getMessage(), ex.getCause());
        }
    }
    
    @SuppressWarnings("unchecked")
    protected static Class<? extends InventoryProvider> getImplementation(URL resource, ClassLoader cl) throws PluginException {
        try {
            InputStream is = null;
            try {
                is = resource.openStream();
                if (is == null) throw new PluginLoadException("Descriptor resource could not be opened.", new NullPointerException());
            } catch (IOException ioe) {
                throw new PluginLoadException("Failed to load plugin descriptor due to I/O exception.", ioe);
            }
            
            Reader input = new InputStreamReader(is);
            LineNumberReader lnr = new LineNumberReader(input);
            String impl = null;
            try {
                impl = lnr.readLine();
            } finally {
                lnr.close();
                input.close();
            }
            
            if (impl == null) throw new PluginLoadException("No provider information found (unexpected end of stream reading descriptor).");
            impl = impl.trim();
            Class<?> providerImpl = cl.loadClass(impl);
            if (InventoryProvider.class.isAssignableFrom(providerImpl)) {
                return (Class<? extends InventoryProvider>) providerImpl;
            } else {
                throw new PluginLoadException("Plugin must implement InventoryProvider interface.");
            }
        } catch (IOException ioe) {
            throw new PluginLoadException("No provider information found.", ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new PluginLoadException("Unable to load provider implementation.", cnfe);
        }
    }
    
    /**
     * Directly register a provider implementation.  This is useful if we want
     * to bundle plugins within EFD itself and make them mandatory.
     * @param providerImpl InventoryProvider implementation
     * @return ProviderInfo from the registered provider
     * @throws PluginException
     */
    public static ProviderInfo register(Class<? extends InventoryProvider> providerImpl) throws PluginException {        
        registeredPlugins.putIfAbsent(providerImpl.getName(), 
            newProviderInstance(providerImpl));
        return registeredPlugins.get(providerImpl.getName()).getProviderInfo();        
    }
    
    /**
     * Register a plugin archive with the inventory plugin system.
     * @param pluginFile a .jar archive containing the plugin
     * @return the ProviderInfo from the successfully loaded inventory plugin
     * @throws PluginException if there is any problem during registration
     */
    public static ProviderInfo register(File pluginFile) throws PluginException {
        URLClassLoader cl = newJarFileClassLoader(pluginFile);
        URL u = cl.findResource(PLUGIN_DESCRIPTOR_RESOURCE);
        if (u == null) throw new PluginLoadException("The plugin is not valid (descriptor not found): " + pluginFile.getAbsolutePath());
        Class<? extends InventoryProvider> providerImpl = getImplementation(u, cl);
        return register(providerImpl);
    }
    
    /**
     * Find a provider for the inventory represented by the specified file and
     * use it to construct an EFD-compatible Inventory abstraction.
     * @param inventoryResource URI for the backing inventory to read
     * @return EFD-compatible Inventory abstraction.
     * @throws PluginException - wrapper for following types:
     * @throws PluginExecutionException if provider throws an exception while creating an Inventory
     * @throws PluginProviderNotFoundException if no provider claims to handle the specified file 
     */
    public static Inventory newInstance(URI inventoryResource, Map<String, Object> parameters) throws PluginException {
        InventoryProvider ip = getProvider(inventoryResource);
        try {
            return ip.newInstance(inventoryResource, parameters);
        } catch (Exception e) {
            throw new PluginExecutionException("An exception was thrown during execution of the inventory provider " + ip.getProviderInfo().toString(), e);
        }
    }
    
    protected static InventoryProvider getProvider(URI inventoryResource) throws PluginException {
        for (InventoryProvider p : registeredPlugins.values()) {
            try {
                if (p.accept(inventoryResource)) return p;
            } catch (Exception e) {
                log.debug(p.getClass() + " threw an exception during accept method.", e);
            }
        }
        throw new PluginProviderNotFoundException("No provider could be found for " + (inventoryResource != null ? inventoryResource.toASCIIString() : "(null)"));
    }
    
    /**
     * Discover which parameters an inventory implementation might accept.
     * @param inventoryResource The URI of the inventory resource.
     * @return A collection of parameters
     * @throws PluginException
     */
    public static Collection<Parameter> getParameterInfo(URI inventoryResource) throws PluginException {
        InventoryProvider ip = getProvider(inventoryResource);
        return ip.getParameterInfo();
    }
    
    /**
     * Inspect registered inventory plugins.
     * @return a collection of ProviderInfo from all the registered inventory plugins.
     */
    public static Collection<ProviderInfo> getProviderInfo() {
        Set<ProviderInfo> info = new HashSet<ProviderInfo>();
        for (InventoryProvider p : registeredPlugins.values()) {
            info.add(p.getProviderInfo());
        }
        return info;
    }
    
    protected static InventoryProvider newProviderInstance(Class<? extends InventoryProvider> clazz) throws PluginException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException ie) {
            throw new PluginLoadException("Unable to instantiate provider implementation.", ie);
        } catch (IllegalAccessException iae) {
            throw new PluginLoadException("Unable to access provider implementation.", iae);
        } catch (Exception t) {
            // this catches exceptions during static initialization as well as
            // from constructors throwing checked & unchecked exceptions
            throw new PluginLoadException("Unhandled exception occurred during instantiation.", t);
        }
    }
    
    /**
     * Only for unit testing.
     */
    protected static void clearPlugins() {
        registeredPlugins.clear();
    }
}
