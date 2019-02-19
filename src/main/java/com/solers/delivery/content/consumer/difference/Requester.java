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
package com.solers.delivery.content.consumer.difference;

import static com.solers.delivery.event.ContentEvent.ContentEventAction.ADD;
import static com.solers.delivery.event.ContentEvent.ContentEventAction.UPDATE;
import static com.solers.delivery.event.ContentEvent.ContentEventResult.PENDING_GBS;
import static com.solers.delivery.event.ContentEvent.ContentEventResult.REQUEST_FAILED;
import static com.solers.delivery.event.ContentEvent.ContentEventResult.SUCCESS;
import static com.solers.delivery.event.ContentEvent.ContentEventResult.TRANSFER_FAILED;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

import com.solers.delivery.content.status.ConsumerProgress;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.EventManager;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.delivery.transport.http.TransferType;
import com.solers.delivery.transport.http.client.Transfer;
import com.solers.delivery.transport.http.client.TransferContent;
import com.solers.delivery.transport.http.client.TransferContentGBS;
import com.solers.delivery.transport.http.client.TransferService;
import com.solers.util.Callback;

/**
 * Requester class resolves ADD and REFRESH ContentDifferences
 * 
 * @author JGimourginas
 */
@Configurable
public class Requester implements ContentDifferenceHandler {

    private static final Logger log = Logger.getLogger(Requester.class);
    private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled();

    private final ConsumerContentSet contentSet;
    private final TransferService transferService;

    private EventManager eventManager;
    
    public Requester(ConsumerContentSet contentSet, TransferService transferService) {
        this.contentSet = contentSet;
        this.transferService = transferService;
    }
    
    @Autowired
    public void setEventManager(@Qualifier("eventManager") EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * No-op
     * 
     * @see com.solers.delivery.content.consumer.difference.ContentDifferenceHandler#cleanup(com.solers.delivery.content.status.ConsumerProgress)
     */
    @Override
    public void cleanup(ConsumerProgress progress) {
    }

    /**
     * Process the content difference
     * 
     * @see com.solers.delivery.content.consumer.difference.DifferenceHandler#handle(com.solers.delivery.content.consumer.difference.ContentDifference)
     */
    public void handle(ContentDifference difference, ConsumerProgress progress) {
        log.debug("Next difference to resolve: " + difference.getPath());
        boolean success = true;
        if (difference.isDirectory()) {
            success = createDirectory(difference, progress.getKey());
        } else {
            success = requestContent(difference, progress);
        }

        if (difference.getAction() == ContentDifferenceActions.ADD) {
            progress.added(difference.getSize(), success);
        } else {
            progress.updated(difference.getSize(), success);
        }
    }

   /**
     * Create and execute a Transfer for content, which will cause the Transport to retrieve a piece of content and then create or update the corresponding file
     * on the local file system
     * 
     * @param contentToGet
     *            an ADD or UPDATE difference between the file system and the supplier inventory
     * @param syncKey
     */
    protected boolean requestContent(ContentDifference contentToGet, final ConsumerProgress progress) {

        ContentEvent contentEvent = createContentEvent(contentToGet, progress.getKey());
        contentEvent.setMessage("Requested ");

        String fullPath = FilenameUtils.separatorsToUnix(new File(contentSet.getPath(), contentToGet.getPath()).getAbsolutePath());
        Transfer transfer;
        if (contentSet.isSupportsGbsTransport()) {
            transfer = new TransferContentGBS(fullPath, contentToGet.getPath(), contentSet.getSupplierName(), contentSet.getName(), progress.getKey());
        } else {
            transfer = new TransferContent(fullPath, contentToGet.getPath(), contentSet.getSupplierName(), contentToGet.getSize(), progress.getKey());
        }
        
        transfer.setDataReceivedCallback(new Callback<Long>() {
            @Override
            public void call(Long e) {
                progress.updateLastUpdateTime();
            }
        });
        
        transfer.setTransferType(TransferType.ADD);
        if (contentToGet.getAction() == ContentDifferenceActions.REFRESH) {
            transfer.setTransferType(TransferType.UPDATE);
        }

        log.debug("Sending transfer for " + contentToGet.getPath());
        transfer = transferService.process(transfer);
        
        long timeStamp = contentToGet.getTimestamp();
        long parentTimeStamp = transfer == null ? 0L : new File(transfer.getLocalPath()).getParentFile().lastModified();
        return finalizeRequest(transfer, timeStamp, parentTimeStamp, contentEvent);
    }

    /**
     * Logs the status of the transfer. Updates the file system time of a successful transfer.
     * 
     * @param transfer
     *            that was sent
     * @param timeStamp
     *            timestamp that should be set on content added to file system
     */
    private boolean finalizeRequest(Transfer transfer, long timeStamp, long parentTimeStamp, ContentEvent contentEvent) {
        boolean success = false;
        if (transfer != null && transfer.getStatus() != null) {
            Transfer.Status status = transfer.getStatus();
            contentEvent.setBytesManipulated(transfer.getBytesTransferred());
            switch (status) {
                case COMPLETED:
                    if (transfer.getBytesTransferred() != contentEvent.getBytesRequested()) {
                        contentEvent.setResult(TRANSFER_FAILED);
                        success = false;
                    } else {
                        // must set the timestamp on the file now that it exists
                        new File(transfer.getLocalPath()).setLastModified(timeStamp);
                        // must also preserve parent time stamp - necessary for Windows OS only where modifying 
                        // a sub file causes a directory to have its timestamp changed
                        new File(transfer.getLocalPath()).getParentFile().setLastModified(parentTimeStamp);
                        success = true;
                    }
                    break;
                case FAILED_TRANSFER:
                    contentEvent.setResult(TRANSFER_FAILED);
                    success = false;
                    break;
                case PENDING_GBS:
                    contentEvent.setResult(PENDING_GBS);
                    success = true;
                    break;
                default:
                    contentEvent.setResult(REQUEST_FAILED);
                    success = false;
                    break;
            }
        } else {
            contentEvent.setResult(REQUEST_FAILED);
            success = false;
        }
        // for all types of status, stop event and submit to eventManager
        eventManager.received(contentEvent);
        return success;
    }

    /**
     * Creates or updates a directory that does not match the inventory
     * 
     * @param content
     *            ContentDifference representing a directory
     * @param syncKey
     */
    protected boolean createDirectory(ContentDifference content, String syncKey) {
        ContentEvent contentEvent = createContentEvent(content, syncKey);
        contentEvent.setMessage("Requested ");

        File dirToCreate = new File(contentSet.getPath(), content.getPath());
        File dirsParent = dirToCreate.getParentFile();
        long parentTimestamp = dirsParent.lastModified();
        if (IS_DEBUG_ENABLED)
            log.debug("Resolving difference for directory: " + content.getPath());

        boolean success = true;
        if (dirToCreate.exists()) {
            //if the dir/file already exists, delete it first, needed for special cases
            success &= dirToCreate.delete();
        }

        //mkdirs may fail if directories already exist
        success &= dirToCreate.mkdirs();
        if (dirToCreate.exists()) success = true;
       
        // for directory, set the timestamp to same timestamp as on supplier
        dirToCreate.setLastModified(content.getTimestamp());
        // also need to readjust parent's timestamp - this is only for Windows OS where modification to files in a directory change the directories timestamp
        dirsParent.setLastModified(parentTimestamp);

        contentEvent.setResult(ContentEventResult.SUCCESS);
        if (!success) {
            log.error("Failed to create: " + dirToCreate.getPath() + " - Check File System Permissions");
            contentEvent.setResult(ContentEventResult.REQUEST_FAILED);
        }
        eventManager.received(contentEvent);
        return success;
    }

    /**
     * Create and initialize a content event
     * 
     * @param difference
     * @param synchronization
     * @return
     */
    protected ContentEvent createContentEvent(ContentDifference difference, String syncKey) {
        ContentEvent result = new ContentEvent(contentSet.getId());

        if (difference.getAction() == ContentDifferenceActions.ADD) {
            result.setAction(ADD);
        } else {
            result.setAction(UPDATE);
            result.setCurrentSize(new File(contentSet.getPath(), difference.getPath()).length());
        }

        result.setHostname(contentSet.getSupplierAddress());
        result.setContentSetName(contentSet.getName());
        result.setDirectory(difference.isDirectory());
        result.setBytesRequested(difference.getSize());
        result.setPath(FilenameUtils.separatorsToUnix(difference.getPath()));
        result.setSynchronizationId(SynchronizationEvent.getId(syncKey, contentSet.getId()));
        result.setResult(SUCCESS);
        result.setPathMtime(difference.getTimestamp());

        return result;
    }
}
