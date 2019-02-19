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
package com.solers.delivery.transport.gbs.poll;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.solers.delivery.transport.gbs.Archive;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.event.EventManager;
import com.solers.delivery.event.GBSUpdateCompleteEvent;
import com.solers.delivery.event.PendingGBSUpdateEvent;
import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.event.ContentEvent.ContentEventResult;

public class GbsPollListener implements PollListener {

    /* Logger for GbsPollListener */
    private static final Logger log = Logger.getLogger(GbsPollListener.class);

    /* Path Separator */
    private static final String SEPARATOR = System.getProperty("file.separator");
    
    protected final EventManager eventManager;

    public GbsPollListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void onAdd(Archive archive, String path, String syncKey, ContentSet contentSet) {
        String archiveName = archive.getArchive().getName();
        log.debug("Processing file: " + archiveName);
        String dir = path + SEPARATOR;
        
        try {
            archive.extractFilesToPath(dir, new EventUpdateClosure(contentSet, syncKey));
        } catch (IOException e) {
            log.warn("Failed to extract all files in archive " + archiveName + " to path " + path, e);
            return;
        } finally {
            GbsPoller.removeFile(new File(dir + Archive.ARCHIVE));
        }
        
        eventManager.gbsUpdateComplete(new GBSUpdateCompleteEvent(contentSet.getId(), syncKey));

        log.info("Successfully extracted all files in archive " + archiveName + " to path " + path);
    }
    
    class EventUpdateClosure implements ExtractProgress {
        
        private final ContentSet contentSet;
        private final String syncId;
        
        EventUpdateClosure(ContentSet contentSet, String syncId) {
            this.contentSet = contentSet;
            this.syncId = syncId;
        }

        public void success(String path, long size, ContentEventAction action) {
            eventManager.received(new PendingGBSUpdateEvent(contentSet.getId(), contentSet.getName(), syncId, path, size, action, ContentEventResult.SUCCESS));
        }
        
        public void skipped(String path, long size) {
            eventManager.received(new PendingGBSUpdateEvent(contentSet.getId(), contentSet.getName(), syncId, path, size, ContentEventAction.NONE, ContentEventResult.GBS_ALREADY_RECEIVED));
        }
    }
}
