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
package mil.nga.efd.controllers;

import java.util.Calendar;

import mil.nga.efd.domain.PendingDelete;

/**
 * @author JGimourginas
 */
public interface PendingDeleteDAO {
    
	String GET_BY_PATH = "getByPath";
    String GET_NEXT_TO_DELETE = "getNextToDelete";

    /**
     * Find a specific pending delete entry by path
     * 
     * @param consumerContentSetId id of the consumer content set that marked the file for deletion
     * @param path from root to the file to be deleted
     * @return PendingDelete object matching the description
     */
    public PendingDelete getByPath(Long consumerContentSetId, String path);

    /**
     * Get the next item to delete based for this consumerContentSet.  Items returned will have been in the pending
     * list for longer than the delete delay
     * 
     * @param consumerContentSetId of consumer content set that marked the file for deletion
     * @param date that must have passed for all returned results
     * @return PendingDelete object that should be removed from file system
     */
    public PendingDelete getNextToDelete(Long consumerContentSetId, Calendar date);
}
