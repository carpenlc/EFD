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
package com.solers.delivery.transport.http;

import java.util.concurrent.atomic.AtomicLong;

import com.solers.delivery.util.MathHelper;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DefaultTransferStatus implements TransferStatus {

    private final String fileName;
    private final long fileSize;
    private final AtomicLong bytesTransferred;
    private final TransferType type;
    
    public DefaultTransferStatus(String fileName, long fileSize, TransferType type) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.type = type;
        this.bytesTransferred = new AtomicLong(0);
    }

    public void addBytesTransferred(long bytes) {
        bytesTransferred.addAndGet(bytes);
    }

    @Override
    public long getBytesRequested() {
        return fileSize;
    }

    @Override
    public long getBytesTransferred() {
        return bytesTransferred.get();
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public double getPercentComplete() {
        return MathHelper.percentComplete(this.bytesTransferred.get(), this.fileSize);
    }

    @Override
    public TransferType getTransferType() {
        return type;
    }
    
}
