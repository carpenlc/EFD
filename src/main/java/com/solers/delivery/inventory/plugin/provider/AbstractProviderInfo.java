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
 * AbstractProviderInfo is an abstract implemntation of ProviderInfo
 * that provides standard Java methods such as toString, hashCode, 
 * and equals.  It also implements getDeveloper() to return getVendor(),
 * as these will frequently be the same.  It is encouraged that, if possible,
 * this class is extended rather than doing a raw implementation of
 * ProviderInfo.
 * 
 * @author gvanore
 */
public abstract class AbstractProviderInfo implements ProviderInfo {           
    /**
     * Only need to override getDeveloper if different than the vendor.
     */
    @Override
    public String getDeveloper() {
        return getVendor();
    }
    
    @Override
    public String toString() {
        boolean vendorIsDeveloper = getVendor().equals(getDeveloper());
        return (getVendor() + "-" + getName() + "-" + getVersion() +
            (vendorIsDeveloper ? "" : "-" + getDeveloper())).replaceAll("\\s", "");
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof ProviderInfo) {
            ProviderInfo pi = (ProviderInfo) other;
            return (
                this.getVendor().equals(pi.getVendor()) &&
                this.getName().equals(pi.getName()) &&
                this.getVersion().equals(pi.getVersion()) &&
                this.getDeveloper().equals(pi.getDeveloper())
            );
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return (
            this.getVendor() + 
            this.getName() + 
            this.getVersion() + 
            this.getDeveloper()
        ).hashCode();
    }
}
