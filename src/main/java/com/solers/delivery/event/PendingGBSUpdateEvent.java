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

import org.apache.commons.io.FilenameUtils;

import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.event.ContentEvent.ContentEventResult;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PendingGBSUpdateEvent extends BaseEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -5523941969360219436L;
    private long contentSetId;
    private final String contentSetName;
    private final String syncKey;
    private final String path;
    private final long size;
    private final ContentEventAction action;
    private final ContentEventResult result;
    
    public PendingGBSUpdateEvent(long contentSetId, String contentSetName, String syncKey, String path, long size, ContentEventAction action, ContentEventResult result) {
        this.contentSetId = contentSetId;
        this.contentSetName = contentSetName;
        this.syncKey = syncKey;
        this.path = path;
        this.size = size;
        this.action = action;
        this.result = result;
    }
    
    public ContentEvent getContentEvent() {
        ContentEvent event = new ContentEvent(contentSetId);
        event.setSynchronizationId(getSyncId());
        //TODO: Lucene overwrites, rather than updating, events - fix or leave it?
        event.setBytesRequested(size);
        event.setBytesManipulated(size);
        event.setResult(result);
        event.setAction(action);
        event.setPath(FilenameUtils.separatorsToUnix(path));
        event.setContentSetName(contentSetName);
        return event;
    }
    
    public ContentEventResult getResultValue() {
        return result;
    }    
    public String getResult() {
        return result.value();
    }
    
    public String getPath() {
        return path;
    }
    
    public String getSyncId() {
        return SynchronizationEvent.getId(syncKey, contentSetId);
    }
    
    public long getContentSetId() {
        return contentSetId;
    }
    
    public long getSize() {
        return size;
    }
    
    public ContentEventAction getAction() {
        return action;
    }
    public String toString() {
        return String.format("GBS update path %s for synchronization %s contentset %d result %s", path, syncKey, contentSetId, result.value());
    }
}
