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
 * A standard bean that provides information about an inventory plugin.
 * @author gvanore
 */
public interface ProviderInfo {

    /**
     * @return The human name of the inventory system.
     */
    String getName();

    /**
     * @return The version of this inventory plug-in.  This
     * is distinct from the document version handled by the 
     * plug-in.
     */
    String getVersion();

    /**
     * @return The party releasing/supporting development of
     * this plug-in.  Typically a corporation or government
     * entity.
     */
    String getVendor();

    /**
     * @return The party developing this plug-in.  Typically
     * a corporation or government entity.
     */
    String getDeveloper();

}