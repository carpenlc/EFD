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

import java.util.Date;

import com.solers.delivery.content.status.ConsumerProgress;
import com.solers.delivery.content.status.SynchronizationResult;
import com.solers.delivery.util.UUID;
import com.solers.lucene.Converter;
import com.solers.lucene.Index;
import com.solers.lucene.LuceneField;

/**
 * Synchronization event
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */

public class SynchronizationEvent extends BaseEvent {
    
    private static final long serialVersionUID = -3299891438666659569L;
    private final long contentSetId;
    private final String key;
    private final String id;
    
    private transient ConsumerProgress progress;
    private Long elapsedTime;
    private String host;
    private SynchronizationResult result;

    public SynchronizationEvent(String key, long contentSetId) {
        this.key = key;
        this.contentSetId = contentSetId;
        this.id = SynchronizationEvent.getId(key, contentSetId);
        this.elapsedTime = 0L;
    }
    
    public SynchronizationEvent(long contentSetId) {
        this(UUID.newSyncKey(), contentSetId);
    }
    
    public ConsumerProgress getProgress() {
        return progress;
    }
    
    @LuceneField(index=Index.TOKENIZED)
    public String getHost() {
        return host;
    }

    public void setHost(String remoteHost) {
        this.host = remoteHost;
    }
    
    public void setElapsedTime(Long time) {
        elapsedTime = time;
    }
    
    public void setProgress(ConsumerProgress progress) {
        this.progress = progress;
    }
    
    @LuceneField
    public String getId() {
        return id;
    }
    
    public static String getId(String key, long contentSetId) {
        return contentSetId+"-"+key;
    }
    
    public static String getKey(String id) {
        return id.substring(id.indexOf("-")+1);
    }
    
    public String getKey() {
        return this.key;
    }
    
    public long getContentSetId() {
        return contentSetId;
    }

    @LuceneField(index=Index.NO)
    public long getElapsedTime() {
        return elapsedTime;
    }

    @LuceneField(index=Index.NO)
    public long getBytesAdded() {
        return progress == null ? 0 : progress.getBytesAdded();
    }

    @LuceneField(index=Index.NO)
    public long getBytesUpdated() {
        return progress == null ? 0 : progress.getBytesUpdated();
    }

    @LuceneField(index=Index.NO)
    public long getBytesDeleted() {
        return progress == null ? 0 : progress.getBytesDeleted();
    }
    
    @LuceneField(index=Index.NO)
    public long getBytesFailed() {
        return progress == null ? 0 : progress.getFailedBytes();
    }

    @LuceneField(index=Index.NO)
    public long getFilesAdded() {
        return progress == null ? 0 : progress.getFilesAdded();
    }

    @LuceneField(index=Index.NO)
    public long getFilesUpdated() {
        return progress == null ? 0 : progress.getFilesUpdated();
    }

    @LuceneField(index=Index.NO)
    public long getFilesDeleted() {
        return progress == null ? 0 : progress.getFilesDeleted();
    }
    
    @LuceneField(index=Index.NO)
    public long getFailures() {
        return progress == null ? 0 : progress.getFailures();
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("Synchronization for contentSet: ")
              .append(getContentSetId())
              .append(" took ")
              .append(getElapsedTime())
              .append(" milliseconds")
              .append(" Adds: ")
              .append(progress == null ? "N/A" : getFilesAdded())
              .append(" Bytes added: ")
              .append(progress == null ? "N/A" : getBytesAdded())
              .append(" Updates: ")
              .append(progress == null ? "N/A" : getFilesUpdated())
              .append(" Bytes updated: ")
              .append(progress == null ? "N/A" : getBytesUpdated())
              .append(" Deletes: ")
              .append(progress == null ? "N/A" : getFilesDeleted())
              .append(" Bytes deleted: ")
              .append(progress == null ? "N/A" : getBytesDeleted())
              .append(" Failures: ")
              .append(progress == null ? "N/A" : getFailures())
              .append(" Bytes failed: ")
              .append(progress == null ? "N/A" : getBytesFailed());

        return result.toString();
    }
    
    @LuceneField
    public boolean isEmpty() {
        return getFilesAdded() == 0 && getFilesUpdated() == 0 && getFilesDeleted() == 0 && getFailures() == 0;
    }
    
    @LuceneField(converter=Converter.DATE)
    public Date getEndDate() {
        if (elapsedTime == null || elapsedTime == 0) {
            return null;
        }
        return new Date(timestamp + elapsedTime);
    }

    public SynchronizationResult getResult() {
        return result;
    }

    public void setResult(SynchronizationResult result) {
        this.result = result;
    }
    
    public void completed(long elapsedTime) {
        setElapsedTime(elapsedTime);
        computeResult();
    }

    private void computeResult() {
        if (result != null) {
            return;
        }
        
        if (isEmpty()) {
            setResult(SynchronizationResult.COMPLETE_NOT_MODIFIED);
        } else {
            if (getFailures() == 0) {
                setResult(SynchronizationResult.COMPLETE_MODIFIED);
            } else {
                setResult(SynchronizationResult.COMPLETE_WITH_ERRORS);
            }
        }
    }
}
