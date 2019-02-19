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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.GBSUpdateCompleteEvent;
import com.solers.delivery.event.PendingGBSUpdateEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.util.NamedThreadFactory;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class EventLogger implements EventListener {

    private final ExecutorService service = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory());
    
    @Override
    public void consumerSynchronizationCompleted(SynchronizationEvent event) {
        log(event);
    }

    @Override
    public void consumerSynchronizationStarted(SynchronizationEvent event) {
        log(event);
    }

    @Override
    public void received(ContentEvent event) {
        log(event);
    }

    @Override
    public void received(PendingGBSUpdateEvent event) {
        log(event);
    }

    @Override
    public void supplied(ContentEvent event) {
        log(event);
    }

    @Override
    public void supplierSynchronizationCompleted(SynchronizationEvent event) {
        log(event);
    }

    @Override
    public void supplierSynchronizationStarted(SynchronizationEvent event) {
        log(event);
    }
    
    @Override
    public void gbsUpdateComplete(GBSUpdateCompleteEvent event) {
        log(event);
    }
    
    private void log(Object event) {
        service.submit(new LogTask(event));
    }
    
    private static class LogTask implements Runnable {
        
        private static final Logger log = Logger.getLogger(EventLogger.class);
        
        private final Object data;
        
        public LogTask(Object data) {
            this.data = data;
        }
        
        public void run() {
            log.info(data);
        }
    }
}
