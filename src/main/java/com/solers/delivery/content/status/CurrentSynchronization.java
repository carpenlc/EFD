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

import java.util.List;

import com.solers.delivery.transport.http.TransferStatus;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class CurrentSynchronization {
    
    private String id;
    private String host;
    private Long date;
    private String contentSetName;
    private Long contentSetId;
    private Long completedBytes;
    private Long totalBytes;
    private Long itemsCompleted;
    private Long totalItems;
    private Double percentSuccessful;
    private boolean active;
    private List<TransferStatus> currentTransfers;
    private boolean supplier;
    private Long remainingTime;
    private Long elapsedTime;
    private SynchronizationResult result;
    private Long lastUpdateInterval;
    
    public Long getLastUpdateInterval() {
        return lastUpdateInterval;
    }

    public void setLastUpdateInterval(Long lastUpdateInterval) {
        this.lastUpdateInterval = lastUpdateInterval;
    }

    public SynchronizationResult getResult() {
        return result;
    }

    public void setResult(SynchronizationResult result) {
        this.result = result;
    }

    public Long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Long getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(Long remainingTime) {
        this.remainingTime = remainingTime;
    }

    public boolean isSupplier() {
        return supplier;
    }

    public void setSupplier(boolean supplier) {
        this.supplier = supplier;
    }

    public List<TransferStatus> getCurrentTransfers() {
        return currentTransfers;
    }

    public void setCurrentTransfers(List<TransferStatus> currentTransfers) {
        this.currentTransfers = currentTransfers;
    }

    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public Long getDate() {
        return date;
    }
    
    public void setDate(Long date) {
        this.date = date;
    }
    
    public String getContentSetName() {
        return contentSetName;
    }
    
    public void setContentSetName(String contentSetName) {
        this.contentSetName = contentSetName;
    }
    
    public Long getContentSetId() {
        return this.contentSetId;
    }

    public void setContentSetId(Long contentSetId) {
        this.contentSetId = contentSetId;
    }
    
    public Long getCompletedBytes() {
        return completedBytes;
    }
    
    public void setCompletedBytes(Long completedBytes) {
        this.completedBytes = completedBytes;
    }
    
    public Long getTotalBytes() {
        return totalBytes;
    }
    
    public void setTotalBytes(Long totalBytes) {
        this.totalBytes = totalBytes;
    }
    
    public Long getItemsCompleted() {
        return itemsCompleted;
    }
    
    public void setItemsCompleted(Long itemsCompleted) {
        this.itemsCompleted = itemsCompleted;
    }
    
    public Long getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }
    
    public Double getPercentSuccessful() {
        return percentSuccessful;
    }
    
    public void setPercentSuccessful(Double percentSuccessful) {
        this.percentSuccessful = percentSuccessful;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
