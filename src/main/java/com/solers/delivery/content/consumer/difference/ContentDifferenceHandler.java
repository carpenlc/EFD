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
package com.solers.delivery.content.consumer.difference;

import com.solers.delivery.content.status.ConsumerProgress;

/**
 * Handler for Content differences
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public interface ContentDifferenceHandler {
    
    /**
     * Handle a ContentDifference in some fashion
     * 
     * @param difference
     * @param progress
     */
    void handle(ContentDifference difference, ConsumerProgress progress);

    /**
     * Perform any cleanup actions necessary
     * 
     * @param progress
     */
    void cleanup(ConsumerProgress progress);
}
