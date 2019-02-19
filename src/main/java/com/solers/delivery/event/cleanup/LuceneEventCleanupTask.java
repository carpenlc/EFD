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
package com.solers.delivery.event.cleanup;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.Directory;

import com.solers.delivery.lucene.LuceneHelper;
import com.solers.delivery.lucene.SingleFieldSelector;
import com.solers.lucene.Converter;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneEventCleanupTask implements Runnable {
    
    protected static final Logger log = Logger.getLogger(LuceneEventCleanupTask.class);
    
    private final LuceneHelper helper;
    private final Integer daysToKeepContentEvents;
    private final Integer daysTokeepSynchronizationEvents;
    
    public LuceneEventCleanupTask(LuceneHelper helper, Integer daysToKeepContentEvents, Integer daysTokeepSynchronizationEvents) {
        this.helper = helper;
        this.daysToKeepContentEvents = daysToKeepContentEvents;
        this.daysTokeepSynchronizationEvents = daysTokeepSynchronizationEvents;
    }

    public void run() {
        log.info("Running lucene event cleanup task");
        for (File contentSetFile : list(helper.getEventDirectory())) {
            long contentSetId = Long.parseLong(contentSetFile.getName());
            for (File index : list(contentSetFile)) {
                if (index.getName().equals("synchronizations")) {
                    cleanupSynchronizations(contentSetId, index.getName());
                } else {
                    cleanupDetails(contentSetId, index.getName());
                }
            }
        }
    }
    
    private File [] list(File dir) {
        File [] files = dir.listFiles();
        if (files == null) {
            return new File [] {};
        }
        return files;
    }
    
    private void cleanupSynchronizations(long contentSetId, String indexName) {
        cleanup(helper.getIndex(contentSetId, indexName), getSynchronizationEventCutoff());
    }
    
    private void cleanupDetails(long contentSetId, String indexName) {
        cleanup(helper.getIndex(contentSetId, indexName), getContentEventCutoff());
    }
    
    private void cleanup(Directory index, Date cutoff) {
        IndexReader reader = null;
        IndexSearcher searcher = null;
        try {
            if (!IndexWriter.isLocked(index) && IndexReader.indexExists(index)) {
                reader = IndexReader.open(index);
                searcher = new IndexSearcher(reader);
                searcher.search(new MatchAllDocsQuery(), new RemovingHitCollector(reader, cutoff));
            }
        } catch (IOException ex) {
            log.error("Error cleaning up directory: " + index, ex);
        } finally {
            LuceneHelper.close(searcher);
            LuceneHelper.close(reader);
        }
    }
    
    private Date getContentEventCutoff() {
        return getDate(daysToKeepContentEvents);
    }
    
    private Date getSynchronizationEventCutoff() {
        return getDate(daysTokeepSynchronizationEvents);
    }
    
    private Date getDate(Integer days) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - days);
        return calendar.getTime();
    }
    
    private static class RemovingHitCollector extends HitCollector {

        private final IndexReader reader;
        private final FieldSelector selector;
        private final Date date;
        
        public RemovingHitCollector(IndexReader reader, Date date) {
            this.reader = reader;
            this.date = date;
            this.selector = new SingleFieldSelector("timeStamp", FieldSelectorResult.LOAD_AND_BREAK);
        }

        @Override
        public void collect(int docNum, float score) {
           try {
               Document doc = reader.document(docNum, selector);
               Date timestamp = (Date) Converter.DATE.convertFrom(doc.get("timeStamp"));
               if (timestamp != null && timestamp.before(date)) {
                   reader.deleteDocument(docNum);
               }
           } catch (IOException ex) {
               log.error("Error removing record in index: "+reader.directory(), ex);
           }
        }
        
    }
}
