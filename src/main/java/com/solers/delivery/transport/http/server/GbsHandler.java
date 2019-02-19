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
package com.solers.delivery.transport.http.server;

import static com.solers.delivery.transport.http.HTTPHeaders.GBS_RETRIEVAL;
import static com.solers.delivery.transport.http.HTTPStatusCodes.GBS_DELIVERY;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.solers.delivery.content.status.SupplierProgress;
import com.solers.delivery.content.supplier.ContentSetMapper;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.EventManager;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.delivery.transport.gbs.GBSService;
import com.solers.delivery.transport.http.DefaultTransferStatus;
import com.solers.delivery.transport.http.TransferType;
import com.solers.delivery.transport.http.monitor.TransferMonitor;

/**
 * This is a handler class used by the HTTPServer (Jetty) that will handle GBS requests.
 * 
 * @author dthemistokleous
 */
public class GbsHandler extends BaseHandler {
    private static final Logger log = Logger.getLogger(GbsHandler.class.getName());

    private final EventManager eventMgr;
    private final GBSService gbsSupplier;
    private final boolean gbsEnabled;
    
    private TransferMonitor transfers;

    public GbsHandler(boolean enabled, EventManager eventMgr,
                      ContentSetMapper cm, GBSService gbsSupplier) {
        this.gbsEnabled = enabled;
        setCm(cm);
        this.eventMgr = eventMgr;
        this.gbsSupplier = gbsSupplier;
    }
    
    public void setTransferMonitor(TransferMonitor transfers) {
        this.transfers = transfers;
    }

    /**
     * Initialization class called by Spring before any operations are performed on this object
     */
    public void init() {
        Validate.notNull(cm, "Need to supply a ContentSetManager");
        Validate.notNull(eventMgr, "Need to supply an EventManager");
        Validate.notNull(gbsSupplier, "Need to supply a GbsSupplier");
    }

    /**
     * Handles request for Gbs ONLY
     */
    @Transactional
    public void handle(String arg0, HttpServletRequest request, HttpServletResponse response, int arg3) throws IOException, ServletException {
        // GBS is not configured on machine
        if (!gbsEnabled) {
            log.debug("GBS is not configured on this machine, skipping jetty handler");
            return;
        }
        
        String consumerContentSet = request.getHeader(GBS_RETRIEVAL.headerName());

        if (consumerContentSet != null) {
            log.debug("Retrieval via GBS for ConsumerContentSet: " + consumerContentSet);
            String syncKey = getSyncKey(request);
            String supplierContentSet = getContentSetName(request);
            String path = getPathInfo(request);
            log.debug(String.format("Request Path: %s", path));
            
            File file = cm.getFile(supplierContentSet, path);
            long contentSetId = cm.getId(supplierContentSet);
            long length = file.length();
            
            // add file to queue to upload(ftps) to GBS
            gbsSupplier.addFile(consumerContentSet, syncKey, supplierContentSet, request.getRemoteHost(), file, path);
            
            String displayPath = cm.isInventory(path) ? file.getName() : path;
            ContentEvent event = new ContentEvent(contentSetId);
            event.setContentSetName(supplierContentSet);
            //event.setHostname(request.getRemoteAddr());
            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            event.setHostname(ipAddress);
            event.setBytesRequested(length);
            event.setMessage("Supplying via GBS: ");
            event.setAction(ContentEventAction.SUPPLY);
            event.setPath(FilenameUtils.separatorsToUnix(displayPath));
            event.setSynchronizationId(SynchronizationEvent.getId(syncKey, contentSetId));
            event.setResult(ContentEventResult.SENT_GBS);
            event.setBytesManipulated(length);
            event.setPathMtime(file.lastModified());
            event.setDirectory(false);
            eventMgr.supplied(event);
            
            DefaultTransferStatus xfer = new DefaultTransferStatus(path, length, TransferType.SUPPLY); 
            SupplierProgress currentProgress = transfers.getSupplierProgress(event.getSynchronizationId());
            transfers.startTransfer(event.getSynchronizationId(), xfer);
            xfer.addBytesTransferred(length);
            currentProgress.sentBytes(length);
            currentProgress.sentItem();
            transfers.completeTransfer(event.getSynchronizationId(), xfer);

            // needs to be extracted to a super class for ALL handlers
            setRequestHandled(request, response);
            
            response.sendError(GBS_DELIVERY.code(), GBS_DELIVERY.message());
        }
    }
}
