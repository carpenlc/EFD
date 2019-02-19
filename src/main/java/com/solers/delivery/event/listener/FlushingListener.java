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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.solers.delivery.event.ContentEvent;
import com.solers.util.NamedThreadFactory;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public abstract class FlushingListener extends BaseListener {
    
    private final Queue<ContentEvent> events;
    private final int batchSize;
    
    public FlushingListener(int batchSize, int flushTime) {
        this.batchSize = batchSize;
        this.events = new ConcurrentLinkedQueue<ContentEvent>();
        
        ScheduledExecutorService flusher = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory());
        flusher.scheduleAtFixedRate(new FlushTask(), flushTime, flushTime, TimeUnit.SECONDS);
    }
    
    @Override
    public void received(ContentEvent event) {
        add(event);
    }
    
    @Override
    public void supplied(ContentEvent event) {
        add(event);
    }
    
    protected void add(ContentEvent event) {
        events.add(event);
        if (events.size() >= batchSize) {
            flush();
        }
    }
    
    protected void flush() {
        synchronized (events) {
            List<ContentEvent> batch = new ArrayList<ContentEvent>(events.size());
            while (!events.isEmpty()) {
                batch.add(events.remove());
            }
            flush(batch);
        }
        
    }
    
    protected abstract void flush(List<ContentEvent> events);
    
    protected class FlushTask implements Runnable {
        @Override
        public void run() {
            FlushingListener.this.flush();
        }
    }
}
