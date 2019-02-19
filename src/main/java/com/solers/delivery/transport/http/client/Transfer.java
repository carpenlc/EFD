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

import org.apache.commons.httpclient.HttpMethod;

import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.transport.http.TransferType;
import com.solers.delivery.transport.http.client.util.Session;
import com.solers.delivery.util.MathHelper;
import com.solers.util.Callback;

public abstract class Transfer implements TransferStatus {

    protected final String localPath;
    protected final String remotePath;
    protected final String contentSetName;
    protected final String syncId;
    
    private Status status = Status.PENDING;
    private long bytesTransferred = 0;
    private TransferType type;
    private Session session;
    private Callback<Long> dataReceivedCallback;
    
    public Transfer(String localPath, String remotePath, String contentSetName, String syncId) {
        super();
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.contentSetName = contentSetName;
        this.syncId = syncId;
    }
    
    public Session getSession() {
        return session;
    }
    
    public void setSession(Session session) {
        this.session = session;
    }

    public abstract HttpMethod getHttpMethod();

    @Override
    public TransferType getTransferType() {
        return type;
    }

    public void setTransferType(TransferType type) {
        this.type = type;
    }

    @Override
    public String getFileName() {
        return this.localPath;
    }

    @Override
    public double getPercentComplete() {
        return MathHelper.percentComplete(getBytesTransferred(), getBytesRequested());
    }
    
    public void setBytesTransferred(long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
    }
    
    public void addBytesTransferred(long bytes) {
        this.bytesTransferred += bytes;
        if (dataReceivedCallback != null) {
            dataReceivedCallback.call(bytes);
        }
    }
    
    public long getBytesTransferred() {
        return this.bytesTransferred;
    }

    public Status getStatus() {
        return status;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    
    public void setStatusCleanup(Status status) {
        setStatus(status);
        cleanup();
    }
    
    public void cleanup() {
        
    }
    
    public boolean failed() {
        return getStatus().isFailure();
    }
    
    public void setDataReceivedCallback(Callback<Long> dataReceivedCallback) {
        this.dataReceivedCallback = dataReceivedCallback;
    }

    public enum Status {

        PENDING(false),
        RUNNING(false),
        RETRY(false),
        COMPLETED(false),
        COMPLETE_NOT_MODIFIED(false),
        FAILED_TRANSFER(true),
        FAILED_NO_CONNECTION(true),
        FAILED_INVALID_SUPPLIER(true),
        FAILED_SUPPLIER_IS_DISABLED(true),
        FAILED_UNAUTHORIZED(true),
        PENDING_GBS(false);
        
        private boolean failure;
        
        private Status(boolean failure) {
            this.failure = failure;
        }
        
        public boolean isFailure() {
            return failure;
        }
    }
    
}