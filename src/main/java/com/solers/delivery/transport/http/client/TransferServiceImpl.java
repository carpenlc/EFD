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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.management.TransferServiceStatus;
import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.transport.http.client.connection.Connection;
import com.solers.delivery.transport.http.client.retry.RetryPolicyLinearTime;
import com.solers.delivery.transport.http.client.util.CompressionRules;
import com.solers.delivery.transport.http.client.util.ContentCopy;
import com.solers.delivery.transport.http.client.util.Session;
import com.solers.delivery.transport.http.client.util.TransferTask;
import com.solers.delivery.transport.http.monitor.TransferMonitor;
import com.solers.jmx.registry.AutoRegister;
import com.solers.jmx.registry.AutoRegisterObjectName;
import com.solers.util.NamedThreadFactory;

/**
 * @author DBailey
 * 
 */
@Configurable
@AutoRegister(category="Transfer Services")
public class TransferServiceImpl implements TransferService, TransferServiceStatus {

    private static final Logger log = Logger.getLogger(TransferServiceImpl.class);
    
    private static final int IDLE_THREAD_TIME = 300;
    private static final int CONNCURRENT_CONNECTIONS = 3;

    private final ThreadPoolExecutor service;
    private final String contentSetName;
    private final String host;
    private final int port;
    private final Queue<TransferStatus> statuses;
    private final Semaphore saturationLimit;
    
    private TransferMangement tm;
    private CompressionRules compressionRules;
    private TransferMonitor transferMonitor;
    private HttpClient client;
    private boolean isRunning = false;
    private Session currSession;
    
    public TransferServiceImpl(String contentSetName, String host, int port) {
        this.host = host;
        this.port = port;
        this.contentSetName = contentSetName;
        saturationLimit = new Semaphore(CONNCURRENT_CONNECTIONS);
        service = new ThreadPoolExecutor(CONNCURRENT_CONNECTIONS, Integer.MAX_VALUE, IDLE_THREAD_TIME, SECONDS, new SynchronousQueue<Runnable>());
        statuses = new ConcurrentLinkedQueue<TransferStatus>();
    }
    
    @Override
    public String toString() {
        return String.format("[CS: %s  Sync: %s]", contentSetName, (currSession == null) ? "N/A" :currSession.getSyncId());
    }
    
    @PostConstruct
    public void init() {
        Connection conn = new Connection(host, port);
        conn.initialize();
        this.client = conn.getClient();
    }
    
    @Autowired
    public void setTransferMonitor(@Qualifier("transferMonitor") TransferMonitor transferMonitor) {
        this.transferMonitor = transferMonitor;
    }
    
    @Autowired
    public void setCompressionRules(@Qualifier("compressionRules") CompressionRules compressionRules) {
        this.compressionRules = compressionRules;
    }

    @AutoRegisterObjectName
    public String getId() {
        return this.contentSetName + "-" + this.host + "-" + this.port;
    }
    
    @Override
    public boolean isRunning() {
        return this.isRunning;
    }
    
    @Override
    public Transfer process(Transfer transfer) {
        if (!isRunning()){ 
            throw new TransportLifeCycleException("process called on service that is not started");
        }
        
        try {
            saturationLimit.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (transfer instanceof TransferContent) {
            ((TransferContent) transfer).addCompressionRules(compressionRules);
            this.statuses.add(transfer);
        }
        transfer.setSession(currSession);
        Callable<Transfer> task = getTask(transfer);
        Future<Transfer> future = service.submit(task);

        try {
            transfer = future.get();
        } catch (InterruptedException e) {
            future.cancel(true);
            log.warn("TransferTask interrupted during sync " + currSession.getSyncId() + " for content set: " + contentSetName);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("TransferTask encountered an error during sync " + currSession.getSyncId() + " for content set: " + contentSetName, e);
        }
        this.statuses.remove(transfer);
        saturationLimit.release();
        return transfer;
    }

    @Override
    public List<TransferStatus> getCurrentTransfers() {
        return new ArrayList<TransferStatus>(statuses);
    }
    
    @Override
    public void start(SynchronizationEvent event) {

        // Must start monitor before interacting with transport.
        transferMonitor.startConsumer(event);
        
        if (!isRunning()) {
            this.tm = new TransferMangement(getClient());
            try {
                String remoteVersion = tm.sendHandshake();
                currSession = new Session(contentSetName, event.getKey(), remoteVersion);
                tm.sendStart(currSession);
            } catch (TransportUnavailableException e) {
                this.isRunning = false;
                throw e;
            }
            this.isRunning = true;
            service.setThreadFactory(new NamedThreadFactory("Transfer:" + this.toString()));
        } else {
            throw new TransportLifeCycleException("Start called when already running");
        }
    }
    
    @Override
    public void stop(SynchronizationEvent event) {
        transferMonitor.completeConsumer(event);
        if (isRunning()) {
            // Wait until no more transfers are active
            saturationLimit.acquireUninterruptibly(CONNCURRENT_CONNECTIONS);
            this.isRunning = false;
            try {
                tm.sendStop(currSession);
                this.getClient().getHttpConnectionManager().closeIdleConnections(0);
            } catch (TransportUnavailableException e) {
                log.error("Unable to send Stop", e);
            }
            saturationLimit.release(CONNCURRENT_CONNECTIONS);
            this.currSession = null;
        }
    }

    @Override
    public void sendMetrics(long totalRequests, long totalBytes) {
        tm.sendMetrics(currSession, totalRequests, totalBytes);
    }
    
    protected HttpClient getClient() {
        return client;
    }
    
    protected Callable<Transfer> getTask(Transfer transfer) {
        return new TransferTask(client, transfer, new ContentCopy(), new RetryPolicyLinearTime());
    }
}
