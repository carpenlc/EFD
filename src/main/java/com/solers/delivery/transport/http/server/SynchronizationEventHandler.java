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

import static com.solers.delivery.transport.http.HTTPParameters.MGT_TYPE;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.solers.delivery.content.status.SupplierProgress;
import com.solers.delivery.content.status.SynchronizationResult;
import com.solers.delivery.event.EventManager;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.transport.http.HTTPParameters;
import com.solers.delivery.transport.http.client.methods.ManagementMethod;
import com.solers.delivery.transport.http.monitor.TransferMonitor;

public class SynchronizationEventHandler extends BaseHandler {

    private static final Logger log = Logger.getLogger(SynchronizationEventHandler.class.getName());

    /* Event Manager to record Events */
    private final EventManager eventMgr;
    private final TransferMonitor transferMonitor;

    public SynchronizationEventHandler(EventManager eventMgr, TransferMonitor transferMonitor) {
        this.eventMgr = eventMgr;
        this.transferMonitor = transferMonitor;
    }

    public void handle(String arg0, HttpServletRequest request, HttpServletResponse response, int arg3) throws IOException, ServletException {

        if (request.getMethod().equalsIgnoreCase("PUT")) {
            String syncKey = getSyncKey(request);
            String syncEvent = request.getParameter(MGT_TYPE.parameterName());
            String contentSetName = getContentSetName(request);
            long contentSetId = cm.getId(contentSetName);

            log.debug(String.format("Sync Event: %s  for Id %s", syncEvent, syncKey));
            
            ManagementMethod.Type type = ManagementMethod.Type.valueOf(syncEvent);
            
            SynchronizationEvent event = new SynchronizationEvent(syncKey, contentSetId);
            //event.setHost(request.getRemoteAddr());
            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
               ipAddress = request.getRemoteAddr();
            }
            event.setHost(ipAddress);
            if (type == ManagementMethod.Type.METRICS) {
                long items = Long.parseLong(request.getParameter(HTTPParameters.MGT_TOTAL_REQUESTS.parameterName()));
                long bytes = Long.parseLong(request.getParameter(HTTPParameters.MGT_TOTAL_BYTES.parameterName()));
                transferMonitor.startSupplier(event, new SupplierProgress(bytes, items));
            } else if (type == ManagementMethod.Type.START) {
                eventMgr.supplierSynchronizationStarted(event);
                log.info("Supplier synchronization started: "+syncKey+" for content set: "+contentSetName);
            } else {
                event.setResult(getResult(event));
                transferMonitor.completeSupplier(event);
                eventMgr.supplierSynchronizationCompleted(event);
                log.info("Supplier synchronization completed: "+syncKey+" for content set: "+contentSetName);
            }
            // needs to be extracted to a super class for ALL handlers
            setRequestHandled(request, response);
        }
        
    }
    
    private SynchronizationResult getResult(SynchronizationEvent event) {
        SupplierProgress p = transferMonitor.getSupplierProgress(event.getId());
        return p == null ? null : p.getResult();
    }
}
