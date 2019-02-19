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

import static com.solers.delivery.transport.http.HTTPHeaders.ACCEPT_ENCODING;
import static com.solers.delivery.transport.http.HTTPHeaders.CONTENT_ENCODING;
import static com.solers.delivery.transport.http.HTTPHeaders.CONTENT_LENGTH;
import static com.solers.delivery.transport.http.HTTPHeaders.CONTENT_RANGE;
import static com.solers.delivery.transport.http.HTTPHeaders.INVENTORY_TIMESTAMP;
import static com.solers.delivery.transport.http.HTTPHeaders.RANGE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.mortbay.jetty.EofException;
import org.mortbay.util.TypeUtil;

import com.solers.delivery.content.status.SupplierProgress;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.EventManager;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.delivery.transport.http.DefaultTransferStatus;
import com.solers.delivery.transport.http.HTTPRangeHeader;
import com.solers.delivery.transport.http.TransferType;
import com.solers.delivery.transport.http.monitor.TransferMonitor;
import com.solers.util.Callback;
import com.solers.util.StreamCopier;

public class FileHandler extends BaseHandler {

    private static Logger log = Logger.getLogger(FileHandler.class.getName());

    private EventManager eventMgr;
    private TransferMonitor transfers;

    public void setEventMgr(EventManager eventMgr) {
        this.eventMgr = eventMgr;
    }
    
    public void setTransferMonitor(TransferMonitor transfers) {
        this.transfers = transfers;
    }

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
        String contentSetName = getContentSetName(request);
        String path = getPathInfo(request);
        final long contentSetId = cm.getId(contentSetName);
        File file = cm.getFile(contentSetName, path, getTimestamp(request));
        ContentEvent event = createEvent(request, file, contentSetId, path);
        event.setContentSetName(contentSetName);
        final SupplierProgress currentProgress = transfers.getSupplierProgress(event.getSynchronizationId());
        
        long length = file.length();
        long startOffset = 0;
        if (request.getHeader(RANGE.headerName()) != null) {
            HTTPRangeHeader range = HTTPRangeHeader.parse(request.getHeader(RANGE.headerName()));
            if (length - range.getFirstPos() > 0) {
                startOffset = range.getFirstPos();
                length = length - startOffset;
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            }
        }
        response.setHeader(CONTENT_RANGE.headerName(), startOffset + "-" + file.length() + "/" + length);
        event.setBytesRequested(length);

        final DefaultTransferStatus xfer = new DefaultTransferStatus(path, length, TransferType.SUPPLY); 
        transfers.startTransfer(event.getSynchronizationId(), xfer);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = response.getOutputStream();

            if (gzipping(request)) {
                response.setHeader(CONTENT_ENCODING.headerName(), CONTENT_ENCODING.defaultValue());
                out = new GZIPOutputStream(out);
                // Do not set content-length header for gzip content-encoding, 
                // Jetty has support for determining proper transfer-type (i.e. chunked)   
            } else {
                response.setHeader(CONTENT_LENGTH.headerName(), TypeUtil.toString(length));
            }
            
            final StreamCopier copier = new StreamCopier(in, out);
            Callback<ContentSet> listener = new Callback<ContentSet>() {
                public void call(ContentSet cs) {
                    if (cs.getId() == contentSetId) {
                        copier.stop();
                    }
                }
            };
            cm.addDisabledListener(listener);

            try {
                long skipped = in.skip(startOffset);
                if (skipped != startOffset) {
                    log.error("Tried to skip: "+startOffset+" only skipped: "+skipped);
                }
                Callback<Integer> cb = new Callback<Integer>() {
                    public void call(Integer copied) {
                        xfer.addBytesTransferred(copied);
                        if (currentProgress != null) {
                            currentProgress.sentBytes(copied);
                        }
                    }
                };
                copier.copy(cb);
                if (out instanceof GZIPOutputStream) {
                    ((GZIPOutputStream) out).finish();
                }
                if (xfer.getBytesTransferred() != length) {
                    event.setResult(ContentEventResult.TRANSFER_FAILED);
                } else {
                    event.setResult(ContentEventResult.SUCCESS);
                    if (currentProgress != null) {
                        currentProgress.sentItem();
                    }
                }
            } finally {
                cm.removeDisabledListener(listener);
            }
        } catch (EofException e) {
            log.error(String.format("Possible cancel on Consumer [id: %s] for %s", event.getSynchronizationId(), request.getPathInfo()));
            event.setResult(ContentEventResult.TRANSFER_FAILED);
        } catch (IOException e) {
            log.error(String.format("Problem streaming to Consumer [id: %s] for %s", event.getSynchronizationId(), request.getPathInfo()), e);
            event.setResult(ContentEventResult.TRANSFER_FAILED);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
            event.setBytesManipulated(xfer.getBytesTransferred());
            eventMgr.supplied(event);
            transfers.completeTransfer(event.getSynchronizationId(), xfer);
        }
    }
    
    private boolean gzipping(HttpServletRequest request) {
        String header = request.getHeader(ACCEPT_ENCODING.headerName());
        return CONTENT_ENCODING.defaultValue().equals(header);
    }
    
    private long getTimestamp(HttpServletRequest request) {
        long timestamp = 0;
        String tsheader = request.getHeader(INVENTORY_TIMESTAMP.headerName());
        try {
            if (tsheader != null) timestamp = Long.parseLong(tsheader);
        } catch (NumberFormatException nfe) {
            log.error("Could not parse timestamp from " + tsheader);
        }
        return timestamp;
    }
    
    private ContentEvent createEvent(HttpServletRequest request, File file, long contentSetId, String path) {
        String displayPath = cm.isInventory(path) ? file.getName() : path;
        String syncKey = getSyncKey(request);
        
        ContentEvent event = new ContentEvent(contentSetId);
        
        //event.setHostname(request.getRemoteAddr());
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        event.setHostname(ipAddress);
        event.setMessage("Supplying: ");
        event.setAction(ContentEventAction.SUPPLY);
        event.setPath(FilenameUtils.separatorsToUnix(displayPath));
        event.setSynchronizationId(SynchronizationEvent.getId(syncKey, contentSetId));
        event.setDirectory(false);
        event.setPathMtime(file.lastModified());
        
        return event;
    }
}
