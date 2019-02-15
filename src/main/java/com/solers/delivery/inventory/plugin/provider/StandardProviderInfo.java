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

/**
 * Standard implementation of ProviderInfo; instantiate into a
 * static final constant in your InventoryProvider.
 * @author gvanore
 */
public final class StandardProviderInfo extends AbstractProviderInfo {
    private final String name;
    private final String vendor;
    private final String version;
    private final String developer;
    
    public StandardProviderInfo(String vendor, String name, String version) {
        this(vendor, name, version, vendor);
    }
    
    public StandardProviderInfo(String vendor, String name, String version, String developer) {
        this.vendor = vendor;
        this.name = name;
        this.version = version;
        this.developer = developer;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVendor() {
        return this.vendor;
    }

    @Override
    public String getVersion() {
        return this.version;
    }
    
    @Override
    public String getDeveloper() {
        return this.developer;
    }
}
