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
package com.solers.delivery.transport.http.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.solers.delivery.content.consumer.ConsumerContentSetManager;
import com.solers.delivery.content.status.CurrentSynchronization;
import com.solers.delivery.content.status.Progress;
import com.solers.delivery.content.status.SupplierProgress;
import com.solers.delivery.content.status.SynchronizationResult;
import com.solers.delivery.content.supplier.SupplierContentSetManager;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.management.ConsumerStatus;
import com.solers.delivery.management.StatusService;
import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.util.MathHelper;
import com.solers.delivery.web.util.TimeToLiveMap;
import com.solers.util.StopWatch;
import com.solers.util.unit.TimeIntervalUnit;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class TransferMonitor {
    
    private final Map<String, Queue<TransferStatus>> transfers;
    private final TimeToLiveMap<String, SupplierValue> suppliers;
    private final TimeToLiveMap<String, ConsumerValue> consumers;
    
    private ConsumerContentSetManager consumerManager;
    private SupplierContentSetManager supplierManager;
    private StatusService statusService;
    
    public TransferMonitor() {
        transfers = new ConcurrentHashMap<String, Queue<TransferStatus>>();
        suppliers = new TimeToLiveMap<String, SupplierValue>(Long.MAX_VALUE);
        consumers = new TimeToLiveMap<String, ConsumerValue>(Long.MAX_VALUE);
    }
    
    public void setConsumerManager(ConsumerContentSetManager consumerManager) {
        this.consumerManager = consumerManager;
    }

    public void setSupplierManager(SupplierContentSetManager supplierManager) {
        this.supplierManager = supplierManager;
    }

    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }
    
    public void startConsumer(SynchronizationEvent event) {
        ConsumerStatus status = statusService.getConsumerStatus(event.getContentSetId());
        String name = consumerManager.getName(event.getContentSetId());
        consumers.put(event.getId(), new ConsumerValue(event, name, status));
    }
    
    public void startSupplier(SynchronizationEvent event, SupplierProgress progress) {
        transfers.put(event.getId(), new ConcurrentLinkedQueue<TransferStatus>());
        String name = supplierManager.getName(event.getContentSetId());
        suppliers.put(event.getId(), new SupplierValue(event, name, progress));
    }
    
    public void completeConsumer(SynchronizationEvent event) {
        ConsumerValue value = consumers.get(event.getId());
        value.deactivate(event.getResult());
        consumers.expireKey(event.getId(), TimeIntervalUnit.MINUTES, 3);
    }
    
    public void completeSupplier(SynchronizationEvent event) {
        SupplierValue value = suppliers.get(event.getId());
        if (value != null) {
            value.deactivate(event.getResult());
            suppliers.expireKey(event.getId(), TimeIntervalUnit.MINUTES, 3);
            transfers.remove(event.getId());
        }
    }
    
    public void startTransfer(String syncId, TransferStatus xfer) {
        Queue<TransferStatus> q = transfers.get(syncId);
        if (q != null) {
            q.add(xfer);
        }
    }
    
    public void completeTransfer(String syncId, TransferStatus xfer) {
        Queue<TransferStatus> q = transfers.get(syncId);
        if (q != null) {
            q.remove(xfer);
        }
    }
    
    /**
     * @return All active supplier and consumer synchronizations
     */
    public List<CurrentSynchronization> getCurrentSynchronizations() {
        List<CurrentSynchronization> result = new ArrayList<CurrentSynchronization>(consumers.size() + suppliers.size());
        
        for (ConsumerValue v : consumers.values()) {
            result.add(v.toSync(true));
        }
        
        for (SupplierValue v : suppliers.values()) {
            result.add(v.toSync(true));
        }
        return result;
    }
    
    /**
     * @param syncId
     * @return The current progress for the given supplier
     */
    public SupplierProgress getSupplierProgress(String syncId) {
        SupplierValue v = suppliers.get(syncId);
        if (v == null) {
            return null;
        }
        return (SupplierProgress) v.progress;
    }
    
    /**
     * @param supplierId
     * @return A list of the current consumers who are synchronizing from the given supplier
     */
    public List<CurrentSynchronization> getCurrentConsumers(Long supplierId) {
        List<CurrentSynchronization> result = new ArrayList<CurrentSynchronization>();
        for (SupplierValue v : suppliers.values()) {
            if (v.contentSetId == supplierId) {
                CurrentSynchronization sync = v.toSync(true);
                sync.setCurrentTransfers(transfersFor(sync.getId()));
                result.add(sync);
            }
        }
        return result;
    }
    
    private List<TransferStatus> transfersFor(String syncId) {
        Queue<TransferStatus> xfers = transfers.get(syncId);
        if (xfers != null) {
            return new ArrayList<TransferStatus>(xfers);
        }
        return null;
    }
    
    private static class SupplierValue {
        final long contentSetId;
        final String name;
        final long date;
        final String host;
        final String id;
        final Progress progress;
        final StopWatch watch;
        
        boolean active;
        SynchronizationResult result;
        
        protected SupplierValue(SynchronizationEvent event, String name, Progress progress) {
            this.progress = progress;
            date = event.getTimestamp();
            host = event.getHost();
            contentSetId = event.getContentSetId();
            this.name = name;
            active = true;
            id = event.getId();
            watch = new StopWatch().start();
        }
        
        protected void deactivate(SynchronizationResult result) {
            this.result = result;
            active = false;
            watch.stop();
        }
        
        protected CurrentSynchronization toSync(boolean showFull) {
            CurrentSynchronization result = new CurrentSynchronization();
            
            result.setContentSetName(name);
            result.setActive(active);
            result.setId(id);
            result.setSupplier(true);
            result.setContentSetId(contentSetId);
            result.setElapsedTime(watch.getElapsedTime());
            result.setHost(host);
            result.setPercentSuccessful(progress.getPercentComplete());
            result.setLastUpdateInterval(progress.getLastUpdateTime() == 0 ? 0 : System.currentTimeMillis() - progress.getLastUpdateTime());
            
            if (active) {
                long remaining = MathHelper.remainingTime(watch.getElapsedTime(), progress.getCompletedBytes(), progress.getTotalBytes() - progress.getCompletedBytes());
                result.setRemainingTime(remaining);
            } else {
                result.setResult(this.result);
            }
            
            if (showFull) {
                result.setTotalBytes(progress.getTotalBytes());
                result.setCompletedBytes(progress.getCompletedBytes());
                result.setDate(date);
                result.setItemsCompleted(progress.getCompletedItems());
                result.setTotalItems(progress.getTotalItems());
            }
            
            return result;
        }
    }
    
    private static class ConsumerValue extends SupplierValue {
        private final ConsumerStatus status;
        
        protected ConsumerValue(SynchronizationEvent event, String name, ConsumerStatus status) {
            super(event, name, event.getProgress());
            this.status = status;
        }


        protected CurrentSynchronization toSync(boolean showFull) {
            CurrentSynchronization result = super.toSync(showFull);
            
            if (active) {
                if (showFull) {
                    result.setCompletedBytes(status.getBytesAdded() + status.getBytesDeleted() + status.getBytesUpdated());
                    result.setItemsCompleted(status.getItemsAdded() + status.getItemsDeleted() + status.getItemsUpdated());
                }
                result.setPercentSuccessful(status.getPercentCompleted());
                result.setRemainingTime(status.getRemainingTime());
            }
            
            result.setSupplier(false);
            
            return result;
        }
    }
}
