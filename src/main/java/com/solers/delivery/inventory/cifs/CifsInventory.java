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

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jcifs.smb.NtlmPasswordAuthentication;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.node.FilteredNode;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.Filter;

public class CifsInventory implements Inventory {
    protected static final String USER = "user";
    protected static final String PASS = "password";
    protected static final String DOMAIN = "domain";
    
    private static final Set<String> propertyKeys = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(USER, PASS, DOMAIN)));
    
    private final Map<String, String> properties = 
        new HashMap<String, String>(propertyKeys.size());
    
    private final URL rootURL;
    private final String rootName;
    
    private Filter<Node> filter;
    
    public CifsInventory(URL rootURL, String domain, String user, String password) {
        if (!rootURL.getProtocol().equals("smb"))
            throw new IllegalArgumentException("Expecting smb:// URL.");
        
        this.rootURL = rootURL;
        this.rootName = extractRootName(rootURL);
        properties.put(DOMAIN, domain);
        properties.put(USER, user);
        properties.put(PASS, password);
    }
    
    private String extractRootName(URL url) {
        String s = url.getPath();
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        int start = -1;
        if ((start = s.lastIndexOf("/")) >= 0) {
            return s.substring(start + 1);
        }
        return "";
    }
    
    @Override
    public void close() {
        //no streams are open
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public String getRootName() {
        return rootName;
    }

    @Override
    public Set<String> properties() {
        return propertyKeys;
    }

    @Override
    public Node read(String path) {
        return root().getChild(path);
    }

    @Override
    public Node root() {
        return FilteredNode.applyFilter(new CifsNode(rootURL, createAuth()),
            filter);
    }

    @Override
    public void useFilter(Filter<Node> filter) {
        this.filter = filter;
    }
    
    private NtlmPasswordAuthentication createAuth() {
        return new NtlmPasswordAuthentication(
            properties.get(DOMAIN),
            properties.get(USER),
            properties.get(PASS)
        );
    }

}
