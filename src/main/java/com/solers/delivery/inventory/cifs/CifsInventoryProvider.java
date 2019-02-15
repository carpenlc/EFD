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
package com.solers.delivery.inventory.cifs;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import jcifs.smb.Handler;

import com.solers.delivery.inventory.Inventory;
//import com.solers.delivery.inventory.cifs.CifsInventory;
import com.solers.delivery.inventory.plugin.PluginException;
import com.solers.delivery.inventory.plugin.provider.InventoryParameter;
import com.solers.delivery.inventory.plugin.provider.InventoryProvider;
import com.solers.delivery.inventory.plugin.provider.Parameter;
import com.solers.delivery.inventory.plugin.provider.ProviderInfo;
import com.solers.delivery.inventory.plugin.provider.StandardProviderInfo;

public class CifsInventoryProvider implements InventoryProvider {
    private static final ProviderInfo info = new StandardProviderInfo("NGA", "CIFS/Samba", "jcifs-1.3.18");
    
    private static final InventoryParameter USER = new InventoryParameter(CifsInventory.USER, String.class, false, true, "NTLM authentication user name");
    private static final InventoryParameter DOMAIN = new InventoryParameter(CifsInventory.DOMAIN, String.class, false, true, "NTLM authentication domain");
    private static final InventoryParameter PASS = new InventoryParameter(CifsInventory.PASS, String.class, true, true, "NTLM authentication password");
    
    private static final Collection<Parameter> parameters = Collections.unmodifiableCollection(
        new HashSet<Parameter>(Arrays.asList(USER, DOMAIN, PASS)));
    
    private static final Handler SMB_HANDLER = new Handler();
    
    static {
        jcifs.Config.registerSmbURLHandler();
    }
    
    @Override
    public boolean accept(URI resource) {
        if (resource != null) {
            String scheme = resource.getScheme();
            scheme = scheme == null ? "" : scheme;
            return scheme.equals("smb");
        }
        return false;
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return info;
    }

    @Override
    public Inventory newInstance(URI resource, Map<String, Object> parameters) {
        String user = null;
        String domain = null;
        String pass = null;
        
        try {
            user = (String) USER.verifyAndRetrieve(parameters);
            domain = (String) DOMAIN.verifyAndRetrieve(parameters);
            pass = (String) PASS.verifyAndRetrieve(parameters);
        } catch (PluginException pe) {
            throw new RuntimeException(pe);
        }
        
        try {
            return new CifsInventory(
                new URL(null, resource.toASCIIString(), SMB_HANDLER),
                domain, user, pass);
        } catch (MalformedURLException mue) {
            throw new RuntimeException("Error translating URI to URL.", mue);
        }
    }
    
    @Override
    public Collection<Parameter> getParameterInfo() {
        return parameters;
    }
}
