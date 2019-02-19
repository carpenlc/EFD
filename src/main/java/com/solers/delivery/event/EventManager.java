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
package com.solers.delivery.event;

import java.util.List;

import com.solers.delivery.event.listener.EventListener;

public class EventManager implements EventListener {
    
    private final List<EventListener> listeners;

    public EventManager(List<EventListener> listeners) {
        this.listeners = listeners;
    }
    
    public void addListener(EventListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void consumerSynchronizationStarted(SynchronizationEvent event) {
        for (EventListener listener : listeners) {
            listener.consumerSynchronizationStarted(event);
        }
    }
    
    @Override
    public void consumerSynchronizationCompleted(SynchronizationEvent event) {
        for (EventListener listener : listeners) {
            listener.consumerSynchronizationCompleted(event);
        }
    }
    
    @Override
    public void received(ContentEvent event) {
        for (EventListener listener : listeners) {
            listener.received(event);
        }
    }
    
    @Override
    public void supplied(ContentEvent event) {
        for (EventListener listener : listeners) {
            listener.supplied(event);
        }
    }

    @Override
    public void received(PendingGBSUpdateEvent event) {
        for (EventListener listener : listeners) {
            listener.received(event);
        }
    }
    
    @Override
    public void supplierSynchronizationCompleted(SynchronizationEvent event) {
        for (EventListener listener : listeners) {
            listener.supplierSynchronizationCompleted(event);
        }
    }

    @Override
    public void supplierSynchronizationStarted(SynchronizationEvent event) {
        for (EventListener listener : listeners) {
            listener.supplierSynchronizationStarted(event);
        }
    }

    @Override
    public void gbsUpdateComplete(GBSUpdateCompleteEvent event) {
        for (EventListener listener : listeners) {
            listener.gbsUpdateComplete(event);
        }
    }
    
    
}
