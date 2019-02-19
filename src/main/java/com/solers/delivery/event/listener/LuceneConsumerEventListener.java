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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.lucene.LuceneHelper;
import com.solers.jmx.MonitoredExecutorService;
import com.solers.lucene.DocumentCreator;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneConsumerEventListener extends FlushingListener {
    
    protected static final Logger log = Logger.getLogger(LuceneConsumerEventListener.class);
    
    private final LuceneHelper helper;
    private final ExecutorService service;
    
    public LuceneConsumerEventListener(LuceneHelper helper, int batchSize, int flushTime) {
        super(batchSize, flushTime);
        this.helper = helper;
        service = new MonitoredExecutorService(getClass().getSimpleName());
    }
    
    @Override
    public void supplied(ContentEvent event) {
        
    }

    @Override
    public void consumerSynchronizationCompleted(SynchronizationEvent event) {
        // Ensure that any pending content events are processed ahead of this event
        flush();
        service.submit(new SyncFinishedTask(helper, event));
    }
    
    @Override
    protected void flush(List<ContentEvent> events) {
        service.submit(new LuceneContentEventFlusher(helper, events));
    }
    
    private static class SyncFinishedTask implements Runnable {
        private final LuceneHelper helper;
        private final SynchronizationEvent event;
        
        public SyncFinishedTask(LuceneHelper helper, SynchronizationEvent event) {
            this.helper = helper;
            this.event = event;
        }

        public void run() {
            try {
                helper.addDocument(helper.getIndex(event.getContentSetId()), DocumentCreator.create(event));
            } catch (IOException ex) {
                log.error("An error occured", ex);
            }
            
            if (event.isEmpty()) {
                log.info("Removing index for sync: "+event.getId());
                helper.removeIndex(event.getContentSetId(), event.getId());
            } else {
                helper.optimizeIndex(helper.getIndex(event.getContentSetId(), event.getId()));
            }
        }
    }
}
