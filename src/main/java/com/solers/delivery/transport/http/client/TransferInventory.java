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

import com.solers.delivery.transport.http.HTTPHeaders;

public class TransferInventory extends TransferContent {

    private final long inventoryTimestamp;

    public TransferInventory(String localPath, String contentSetName, long inventoryTimestamp, String syncId) {
        //TODO: figure out how to get correct size of inventory instead of zero
        super(localPath, "/", contentSetName, 0, syncId);
        this.inventoryTimestamp = inventoryTimestamp;
    }
    
    @Override
    public String toString() {
        return String.format("[%s   Status: %s  InventoryTimeStamp: %s]", localPath, getStatus(), inventoryTimestamp);
    }

    @Override
    public HttpMethod getHttpMethod() {
        HttpMethod method = super.getHttpMethod();
        method.addRequestHeader(HTTPHeaders.INVENTORY_TIMESTAMP.headerName(), String.valueOf(inventoryTimestamp));
        return method;
    }

    public boolean isNotModified() {
        return getStatus().equals(Status.COMPLETE_NOT_MODIFIED);
    }
}
