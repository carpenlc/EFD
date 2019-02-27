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
package com.solers.delivery.alerts;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.solers.delivery.domain.Alert;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AlertManager implements AlertListener {
    
    private static final Logger log = Logger.getLogger(AlertManager.class);
    
    private final List<AlertListener> listeners;
    
    public AlertManager(List<AlertListener> listeners) {
        this.listeners = new CopyOnWriteArrayList<AlertListener>();
        if (listeners != null) {
            this.listeners.addAll(listeners);
        }
    }
    
    public void addListener(AlertListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(AlertListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void onAlert(Alert alert) {
        for (AlertListener listener : listeners) {
            try {
                listener.onAlert(alert);
            } catch (RuntimeException ex) {
                log.error("Caught exception from: "+listener, ex);
            }
        }
    }
}
