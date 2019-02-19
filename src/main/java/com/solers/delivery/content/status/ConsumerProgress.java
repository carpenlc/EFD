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
package com.solers.delivery.content.status;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.transport.http.TransferType;
import com.solers.delivery.util.MathHelper;

/**
 * Tracks the progress of a synchronization
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ConsumerProgress implements Progress {

    private static final List<TransferStatus> EMPTY_XFERS = Collections.emptyList();
    
    private volatile Holder adds;
    private volatile Holder updates;
    private volatile Holder deletes;
    
    private String key;
    private long totalNewFiles;
    private long lastUpdateTime;
    
    public ConsumerProgress() {
        this.adds = new Holder(0l, 0l);
        this.updates = new Holder(0l, 0l);
        this.deletes = new Holder(0l, 0l);
    }
    
    public void initialize(long addItems, long addBytes, long updateItems, long updateBytes, long deleteItems, long deleteBytes) {
        this.adds = new Holder(addItems, addBytes);
        this.updates = new Holder(updateItems, updateBytes);
        this.deletes = new Holder(deleteItems, deleteBytes);
    }
    
    public void initialize(long addItems, long addBytes) {
        initialize(addItems, addBytes, 0l, 0l, 0l, 0l);
    }
    
    /**
     * @return Synchronization key
     */
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public void addTotalDelete(long bytes) {
        deletes.incrementTotal(bytes);
        updateLastUpdateTime();
    }
    
    public void updateLastUpdateTime() {
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * @return Percentage of bytes completed
     */
    public double getPercentComplete() {
        return getPercentComplete(EMPTY_XFERS);
    }
    
    /**
     * @param xfers
     * @return The percentage of bytes completed taking into account the number of
     * bytes completed for each transfer in {@code xfers}
     */
    public double getPercentComplete(List<TransferStatus> xfers) {
        BigDecimal completedBytes = new BigDecimal(getCompletedBytes());
        
        for (TransferStatus xfer : xfers) {
            completedBytes = completedBytes.add(new BigDecimal(xfer.getBytesTransferred()));
        }
        return MathHelper.percentComplete(completedBytes, new BigDecimal(getTotalBytes()));
    }
    
    public long getTotalNewFiles() {
        return totalNewFiles;
    }
    
    public void setTotalNewFiles(long value) {
        totalNewFiles = value;
    }
    
    public long getTotalNewBytes() {
        return adds.getTotalBytes() + updates.getTotalBytes();
    }
    
    public long getCompletedItems() {
        return getFilesAdded() + getFilesUpdated() + getFilesDeleted();
    }
    
    public long getTotalItems() {
        return getTotalOperations().longValue();
    }
    
    public long getTotalBytes() {
        return adds.getTotalBytes() + updates.getTotalBytes() + deletes.getTotalBytes();
    }

    public long getFailures() {
        return adds.getFailedOperations() + updates.getFailedOperations() + deletes.getFailedOperations();
    }
    
    public long getFailedBytes() {
        return adds.getFailedBytes() + updates.getFailedBytes() + deletes.getFailedBytes();
    }
    
    /**
     * @return The number of bytes transferred
     */
    public long getCompletedBytes() {
        return getBytesAdded() + getBytesUpdated() + getBytesDeleted();
    }

    /**
     * @return The number of bytes added
     */
    public long getBytesAdded(List<TransferStatus> xfers) {
        long result = adds.getCompletedBytes();
        for (TransferStatus x : xfers) {
            if (x.getTransferType() == TransferType.ADD) {
                result += x.getBytesTransferred();
            }
        }
        
        return result;
    }
    
    public long getBytesAdded() {
        return getBytesAdded(EMPTY_XFERS);
    }
    
    /**
     * @return The number of bytes remaining to be added
     */
    public long getBytesAddedRemaining(List<TransferStatus> xfers) {
        long result = adds.getRemainingBytes();
        for (TransferStatus x : xfers) {
            if (x.getTransferType() == TransferType.ADD) {
                result -= x.getBytesTransferred();
            }
        }
        return result;
    }
    
    public long getBytesAddedRemaining() {
        return getBytesAddedRemaining(EMPTY_XFERS);
    }

    /**
     * @return The number of bytes updated
     */
    public long getBytesUpdated() {
        return getBytesUpdated(EMPTY_XFERS);
        
    }
    
    public long getBytesUpdated(List<TransferStatus> xfers) {
        long result = updates.getCompletedBytes();
        for (TransferStatus xfer : xfers) {
            if (xfer.getTransferType() == TransferType.UPDATE) {
                result += xfer.getBytesTransferred();
            }
        }
        return result;
    }
    
    /**
     * @return The number of bytes remaining to be updated
     */
    public long getBytesUpdatedRemaining(List<TransferStatus> xfers) {
        long result = updates.getRemainingBytes();
        for (TransferStatus xfer : xfers) {
            if (xfer.getTransferType() == TransferType.UPDATE) {
                result -= xfer.getBytesTransferred();
            }
        }
        return result;
    }
    
    public long getBytesUpdatedRemaining() {
        return getBytesUpdatedRemaining(EMPTY_XFERS);
    }

    /**
     * @return The number of bytes deleted
     */
    public long getBytesDeleted() {
        return deletes.getCompletedBytes();
    }
    
    /**
     * @return The number of bytes remaining to be deleted
     */
    public long getBytesDeletedRemaining() {
        return deletes.getRemainingBytes();
    }

    /**
     * @return The number of adds completed
     */
    public long getFilesAdded() {
        return adds.getCompletedOperations();
    }
    
    /**
     * @return The number of adds remaining
     */
    public long getFilesAddedRemaining() {
        return adds.getRemainingOperations();
    }

    /**
     * @return The number of updates completed
     */
    public long getFilesUpdated() {
        return updates.getCompletedOperations();
    }
    
    /**
     * @return The number of updates remaining
     */
    public long getFilesUpdatedRemaining() {
        return updates.getRemainingOperations();
    }

    /**
     * @return The number of deletes completed
     */
    public long getFilesDeleted() {
        return deletes.getCompletedOperations();
    }
    
    /**
     * @return The number of deletes remaining
     */
    public long getFilesDeletedRemaining() {
        return deletes.getRemainingOperations();
    }
    
    /**
     * Record <code>bytes</code> added during synchronization
     * 
     * @param bytes
     * @param success whether successful or not
     */
    public void added(long bytes, boolean success) {
        modify(adds, bytes, success);
    }

    /**
     * Record <code>bytes</code> removed during synchronization
     * 
     * @param bytes
     * @param success whether successful or not
     */
    public void removed(long bytes, boolean success) {
        modify(deletes, bytes, success);
    }

    @Override
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Record <code>updated</code> added during synchronization
     * 
     * @param bytes
     * @param success whether successful or not
     */
    public void updated(long bytes, boolean success) {
        modify(updates, bytes, success);
    }
    
    private void modify(Holder p, long bytes, boolean success) {
        if (success) { 
            p.completed(bytes);
        } else {
            p.failed(bytes);
        }
        updateLastUpdateTime();
    }

    /**
     * @return The total number of operations to perform
     */
    private BigDecimal getTotalOperations() {
        return new BigDecimal(adds.getTotalOperations() + updates.getTotalOperations() + deletes.getTotalOperations());
    }

    /**
     * Data holder class
     * 
     * The instance variables are volatile to ensure that the status is
     * accurately reflected to other threads who may be interested in the status
     * 
     * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
     */
    private static class Holder {

        private final AtomicLong totalItems;
        private final AtomicLong totalBytes;
        private final AtomicLong completedBytes;
        private final AtomicLong failedBytes;
        private final AtomicLong completedItems;
        private final AtomicLong failedItems;

        Holder(long totalFiles, long totalBytes) {
            this.totalItems = new AtomicLong(totalFiles);
            this.totalBytes = new AtomicLong(totalBytes);
            this.completedBytes = new AtomicLong(0);
            this.failedBytes = new AtomicLong(0);
            
            this.completedItems = new AtomicLong(0);
            this.failedItems = new AtomicLong(0);
        }
        
        void incrementTotal(long bytes) {
            totalItems.incrementAndGet();
            totalBytes.addAndGet(bytes);
        }

        void completed(long bytes) {
            completedItems.incrementAndGet();
            completedBytes.addAndGet(bytes);
        }
        
        void failed(long bytes) {
            failedItems.incrementAndGet();
            failedBytes.addAndGet(bytes);
        }
        
        long getRemainingOperations() {
            return getTotalOperations() - getFailedOperations() - getCompletedOperations();
        }
        
        long getRemainingBytes() {
            return getTotalBytes() - getFailedBytes() - getCompletedBytes();
        }

        long getCompletedOperations() {
            return completedItems.get();
        }

        long getCompletedBytes() {
            return completedBytes.get();
        }
        
        long getFailedOperations() {
            return failedItems.get();
        }
        
        long getFailedBytes() {
            return failedBytes.get();
        }
        
        long getTotalOperations() {
            return totalItems.get();
        }
        
        long getTotalFiles() {
            return totalItems.get();
        }
        
        long getTotalBytes() {
            return totalBytes.get();
        }
    }
}
