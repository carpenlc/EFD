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
package com.solers.delivery.event.listener;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.GBSUpdateCompleteEvent;
import com.solers.delivery.event.PendingGBSUpdateEvent;
import com.solers.delivery.event.SynchronizationEvent;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public interface EventListener {
    
    void consumerSynchronizationStarted(SynchronizationEvent event);
    void consumerSynchronizationCompleted(SynchronizationEvent event);
    void supplierSynchronizationStarted(SynchronizationEvent event);
    void supplierSynchronizationCompleted(SynchronizationEvent event);
    void supplied(ContentEvent event);
    void received(ContentEvent event);
    void received(PendingGBSUpdateEvent event);
    void gbsUpdateComplete(GBSUpdateCompleteEvent event);
}
