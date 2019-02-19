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

import com.solers.delivery.alerts.AlertManager;
import com.solers.delivery.content.ContentService;
import com.solers.delivery.content.status.SynchronizationResult;
import com.solers.delivery.domain.Alert;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.domain.Alert.AlertType;
import com.solers.delivery.event.SynchronizationEvent;

/**
 * Publishes alerts for synchronizations that did not complete successfully
 * 
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class FailedSynchronizationAlertPublisher extends BaseListener {

    private final AlertManager manager;
    private final ContentService contentService;

    public FailedSynchronizationAlertPublisher(AlertManager manager, ContentService contentService) {
        this.manager = manager;
        this.contentService = contentService;
    }

    @Override
    public void consumerSynchronizationCompleted(SynchronizationEvent event) {
        checkEvent(event);
    }

    @Override
    public void supplierSynchronizationCompleted(SynchronizationEvent event) {
        checkEvent(event);
    }
    
    private void checkEvent(SynchronizationEvent event) {
        if (synchronizationFailed(event)) {
            sendAlert(event);
        }
    }
    
    private boolean synchronizationFailed(SynchronizationEvent event) {
        SynchronizationResult r = event.getResult();
        if (r != null) {
            return r.isFailure() || r.isWarning();
        }
        return false;
    }
    
    private void sendAlert(SynchronizationEvent event) {
        ContentSet set = contentService.get(event.getContentSetId());
        String message = set.getName()+" failed to synchronize. Reason: "+event.getResult().getMessage();
        manager.onAlert(new Alert(message, AlertType.USER));
    }
}
