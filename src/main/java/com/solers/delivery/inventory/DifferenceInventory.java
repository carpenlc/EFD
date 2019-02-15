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
package com.solers.delivery.inventory;

/**
 * This is a marker interface to identify inventories of 
 * differences from inventories of regular files.  If an inventory
 * is an instance of this class, its elements can be cast to
 * DifferenceNodes.
 * 
 * DifferenceInventory implementations must be sortable by their
 * time stamp, so we also force implementing classes to override
 * the Comparable interface. 
 */
public interface DifferenceInventory extends Inventory, Comparable<DifferenceInventory> {
}
