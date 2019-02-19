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
package com.solers.delivery.transport.http.client;

import java.util.List;

import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.transport.http.TransferStatus;

public interface TransferService {

    Transfer process(Transfer transfer);

    void start(SynchronizationEvent event);

    void stop(SynchronizationEvent event);
    
    void sendMetrics(long totalRequests, long totalBytes);
    
    List<TransferStatus> getCurrentTransfers();
    
    public class TransportUnavailableException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public TransportUnavailableException() {
            super();
        }

        public TransportUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }

        public TransportUnavailableException(String message) {
            super(message);
        }

        public TransportUnavailableException(Throwable cause) {
            super(cause);
        }
    }
    
    public class TransportLifeCycleException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public TransportLifeCycleException() {
            super();
        }

        public TransportLifeCycleException(String message, Throwable cause) {
            super(message, cause);
        }

        public TransportLifeCycleException(String message) {
            super(message);
        }

        public TransportLifeCycleException(Throwable cause) {
            super(cause);
        }
    }
}
