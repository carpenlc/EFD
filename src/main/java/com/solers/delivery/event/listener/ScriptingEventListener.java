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
/**
 * 
 */
package com.solers.delivery.event.listener;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.remoting.RemoteLookupFailureException;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.DeliveryEvent;
import com.solers.delivery.event.GBSUpdateCompleteEvent;
import com.solers.delivery.event.PendingGBSUpdateEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.scripting.event.EventType;
import com.solers.delivery.scripting.service.EventService;

/**
 * @author mchen
 *
 */
public class ScriptingEventListener implements EventListener {

        private static final Logger log = Logger.getLogger(ScriptingEventListener.class);
        private EventService eventService;
        private final boolean listenToConsumerSynchronizationEvents;
        private final boolean listenToSupplierSynchronizationEvents;
        private final boolean listenToReceivedContentEvents;
        private final boolean listenToSuppliedContentEvents;
        
        public ScriptingEventListener(Properties prop) {
            this.listenToConsumerSynchronizationEvents = Boolean.valueOf((String)prop.get("listenToConsumerSynchronizationEvents"));
            this.listenToSupplierSynchronizationEvents = Boolean.valueOf((String)prop.get("listenToSupplierSynchronizationEvents"));
            this.listenToReceivedContentEvents = Boolean.valueOf((String)prop.get("listenToReceivedContentEvents"));
            this.listenToSuppliedContentEvents = Boolean.valueOf((String)prop.get("listenToSuppliedContentEvents"));
        }

        public void setEventService(EventService service) {
            this.eventService = service;
        }

        @Override
        public void consumerSynchronizationCompleted(SynchronizationEvent event) {
            if (this.listenToConsumerSynchronizationEvents) {
                processEvent(event, EventType.COMPLETED_CONSUMER);
            }
        }

        @Override
        public void consumerSynchronizationStarted(SynchronizationEvent event) {
            if (this.listenToConsumerSynchronizationEvents) {
                processEvent(event, EventType.STARTED_CONSUMER);
            }
        }
        
        @Override
        public void gbsUpdateComplete(GBSUpdateCompleteEvent event) {
            //do nothing
        }

        @Override
        public void received(ContentEvent event) {
            if (this.listenToReceivedContentEvents) {
                processEvent(event, EventType.RECEIVED_CONTENT);
            }
        }

        @Override
        public void received(PendingGBSUpdateEvent event) {
            //do nothing
        }

        @Override
        public void supplied(ContentEvent event) {
            if (this.listenToSuppliedContentEvents) {
                processEvent(event, EventType.SUPPLIED_CONTENT);
            }
        }

        @Override
        public void supplierSynchronizationCompleted(SynchronizationEvent event) {
            if (this.listenToSupplierSynchronizationEvents) {
                processEvent(event, EventType.COMPLETED_SUPPLIER);
            }
        }

        @Override
        public void supplierSynchronizationStarted(SynchronizationEvent event) {
            if (this.listenToSupplierSynchronizationEvents) {
                processEvent(event, EventType.STARTED_SUPPLIER);
            }
        }

        private void processEvent(DeliveryEvent event, EventType type) {
            
            try {
                log.debug(event);
                
                //only go thru the expensive process of encrypting the object
                //if the server is up and is ready to consume the event
                if (this.eventService.ready()) {
                    type.sendEvent(this.eventService, event);
                    log.debug("Event sent to sever.");
                } else {
                    log.debug("Event not sent.  Server is not ready to consume the event.");
                }
            } catch (RemoteLookupFailureException ex) {
                //sets isServerUp to false since there's a connection exception
                log.warn("Scripting engine is down - " + ex.getCause().getMessage());
            } catch (Exception ex) {
                //TODO mchen handle individual exceptions?
                log.error(ex);
            }
        }
        
}