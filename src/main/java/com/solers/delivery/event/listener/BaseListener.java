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
public abstract class BaseListener implements EventListener {

    @Override
    public void consumerSynchronizationCompleted(SynchronizationEvent event) {
        
    }

    @Override
    public void consumerSynchronizationStarted(SynchronizationEvent event) {
        
    }

    @Override
    public void gbsUpdateComplete(GBSUpdateCompleteEvent event) {
        
    }

    @Override
    public void received(ContentEvent event) {
        
    }

    @Override
    public void received(PendingGBSUpdateEvent event) {
        
    }

    @Override
    public void supplied(ContentEvent event) {
        
    }

    @Override
    public void supplierSynchronizationCompleted(SynchronizationEvent event) {
        
    }

    @Override
    public void supplierSynchronizationStarted(SynchronizationEvent event) {
        
    }

}
