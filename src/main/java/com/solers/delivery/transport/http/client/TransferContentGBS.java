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
import org.springframework.beans.factory.annotation.Configurable;

import com.solers.delivery.transport.http.client.methods.GbsGetMethod;

@Configurable
public class TransferContentGBS extends Transfer {

    private final String consumerContentSetName;

    public TransferContentGBS(String localPath, String remotePath, String contentSetName, String consumerContentSetName, String syncId) {
        super(localPath, remotePath, contentSetName, syncId);
        this.consumerContentSetName = consumerContentSetName;
    }
    
    @Override
    public String toString() {
        return String.format("[GBS  %s   Status: %s  SyncId:%s]", localPath, getStatus(), syncId);
    }
    
    @Override
    public HttpMethod getHttpMethod() {
        HttpMethod method;
        method = new GbsGetMethod(getSession(), consumerContentSetName, remotePath);
        return method;
    }

    @Override
    public long getBytesTransferred() {
        return 0;
    }

    @Override
    public long getBytesRequested() {
        return 0;
    }
}
