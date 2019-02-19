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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.ContentEventComparator;
import com.solers.delivery.lucene.LuceneHelper;
import com.solers.lucene.DocumentCreator;
import com.solers.lucene.DocumentResult;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneContentEventFlusher implements Runnable {
    
    private static final Logger log = Logger.getLogger(LuceneContentEventFlusher.class);
    private static final Comparator<ContentEvent> SORTER = new ContentEventComparator();
    
    private final LuceneHelper helper;
    private final List<ContentEvent> events;
    private final StackTraceElement [] stack;
    
    public LuceneContentEventFlusher(LuceneHelper helper, List<ContentEvent> events) {
        this.helper = helper;
        this.events = events;
        this.stack = Thread.currentThread().getStackTrace();
    }
    public void run() {
        Collections.sort(events, SORTER);
        
        IndexWriter writer = null;
        String id = "";
        try {
            for (ContentEvent event : events) {
                if (!id.equals(event.getSynchronizationId())) {
                    LuceneHelper.close(writer);
                    id = event.getSynchronizationId();
                    writer = new IndexWriter(helper.getIndex(event.getContentSetId(), event.getSynchronizationId()), new StandardAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
                }
                handle(writer, event);
            }
        } catch (IOException ex) {
            log.error("An error occured", ex);
            Exception t = new Exception();
            t.setStackTrace(stack);
            log.error("Originating stack", t);
        } finally {
            LuceneHelper.close(writer);
        }
    }
    
    protected void handle(IndexWriter writer, ContentEvent event) throws IOException {
        DocumentResult r = DocumentCreator.create(event);
        writer.addDocument(r.getDocument(), r.getAnalyzer());
    }
}
