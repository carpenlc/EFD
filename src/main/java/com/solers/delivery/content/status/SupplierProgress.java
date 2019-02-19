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

import java.util.concurrent.atomic.AtomicLong;

import com.solers.delivery.util.MathHelper;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SupplierProgress implements Progress {

    private final long totalBytes;
    private final long totalItems;
    private final AtomicLong bytes;
    private final AtomicLong items;
    private long lastUpdateTime;
    
    public SupplierProgress(long totalBytes, long totalItems) {
        this.totalBytes = totalBytes;
        this.totalItems = totalItems;
        this.bytes = new AtomicLong(0);
        this.items = new AtomicLong(0);
    }
    
    public void sentBytes(long bytes) {
        this.bytes.addAndGet(bytes);
        lastUpdateTime = System.currentTimeMillis();
    }
    
    public void sentItem() {
        this.items.incrementAndGet();
        lastUpdateTime = System.currentTimeMillis();
    }
    
    public double getPercentComplete() {
        return MathHelper.percentComplete(bytes.get(), totalBytes);
    }
    
    public long getTotalItems() {
        return totalItems;
    }
    
    public long getTotalBytes() {
        return totalBytes;
    }
    
    public long getCompletedBytes() {
        return bytes.get();
    }
    
    public long getCompletedItems() {
        return items.get();
    }
    
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public SynchronizationResult getResult() {
        if (getCompletedItems() == getTotalItems()) {
            if (getTotalItems() > 0) {
                return SynchronizationResult.COMPLETE_MODIFIED;
            } else {
                return SynchronizationResult.COMPLETE_NOT_MODIFIED;
            }
        } else if (getCompletedItems() > getTotalItems()) {
            return SynchronizationResult.COMPLETE_WITH_MISCOUNT;
        } else if (getCompletedBytes() > 0) {
            return SynchronizationResult.INCOMPLETE;
        } else {
            return SynchronizationResult.FAILED;
        }
    }
}
