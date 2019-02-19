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

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.GBSUpdateCompleteEvent;
import com.solers.delivery.event.PendingGBSUpdateEvent;
import com.solers.delivery.lucene.LuceneHelper;
import com.solers.delivery.lucene.OptimizeTask;
import com.solers.jmx.MonitoredExecutorService;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneConsumerGbsListener extends FlushingListener {

    private final LuceneHelper helper;
    protected final ExecutorService flusher;
    
    public LuceneConsumerGbsListener(LuceneHelper helper, int batchSize, int flushTime) {
        super(batchSize, flushTime);
        this.helper = helper;
        flusher = new MonitoredExecutorService(getClass().getSimpleName());
    }
    
    @Override
    public void received(ContentEvent event) {
        
    }

    @Override
    public void supplied(ContentEvent event) {
        
    }

    @Override
    public void received(PendingGBSUpdateEvent event) {
        add(event.getContentEvent());
    }
    
    @Override
    public void gbsUpdateComplete(GBSUpdateCompleteEvent event) {
        flush();
        flusher.submit(new OptimizeTask(helper, helper.getIndex(event.getContentSetId(), event.getSyncId())));
    }
    
    @Override
    protected void flush(List<ContentEvent> events) {
        flusher.submit(new RemovePendingGbsEventsTask(helper, events));
        flusher.submit(new LuceneContentEventFlusher(helper, events));
    }
    
    private static class RemovePendingGbsEventsTask extends LuceneContentEventFlusher {

        public RemovePendingGbsEventsTask(LuceneHelper helper, List<ContentEvent> events) {
            super(helper, events);
        }

        @Override
        protected void handle(IndexWriter writer, ContentEvent event) throws IOException {
            writer.deleteDocuments(new Term("pathKey", event.getPathKey()));
        }
    }
}
