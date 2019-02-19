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

import static com.solers.delivery.transport.http.HTTPHeaders.RANGE;

import java.io.File;

import org.apache.commons.httpclient.HttpMethod;
import org.springframework.beans.factory.annotation.Configurable;

import com.solers.delivery.transport.http.HTTPRangeHeader;
import com.solers.delivery.transport.http.client.methods.ContentGetMethod;
import com.solers.delivery.transport.http.client.methods.GZIPAwareGetMethod;
import com.solers.delivery.transport.http.client.util.CompressionRules;

@Configurable
public class TransferContent extends Transfer {
    
    private final long fileSize;
    
    private boolean compressed = false;
    private File file;

    public TransferContent(final String localPath, String remotePath, String contentSetName, long fileSize, String syncId) {
        super(localPath, remotePath, contentSetName, syncId);
        this.fileSize = fileSize;
    }

    @Override
    public long getBytesRequested() {
        return this.fileSize;
    }

    @Override
    public String toString() {
        return String.format("[%s   Status: %s  Compression: %s]", localPath, getStatus(), compressed);
    }

    @Override
    public HttpMethod getHttpMethod() {
        
        HttpMethod method = (compressed) ? new GZIPAwareGetMethod(getSession(), remotePath) : new ContentGetMethod(getSession(), remotePath);
        if (getBytesTransferred() != 0) {
            HTTPRangeHeader rangeHeader = new HTTPRangeHeader(getBytesTransferred());
            method.setRequestHeader(RANGE.headerName(), rangeHeader.toString());
        }
        return method;
    }

    @Override
    public void cleanup() {
        if (file != null) {
            file.delete();
        }
    }

    public void addCompressionRules(CompressionRules compression) {
        this.compressed = compression.shouldBeCompressed(localPath, fileSize);
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public File getFile() {
        return file;
    }
}
