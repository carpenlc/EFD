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
package com.solers.delivery.transport.http.client.util;

public class Session {
    
    private final String contentSetName;
    private final String syncId;
    private final String remoteVersion;
    
    
    public Session(String contentSetName, String syncId, String remoteVersion) {
        this.contentSetName = contentSetName;
        this.syncId = syncId;
        this.remoteVersion = remoteVersion;
    }
    public String getContentSetName() {
        return contentSetName;
    }
    public String getSyncId() {
        return syncId;
    }
    public String getRemoteVersion() {
        return remoteVersion;
    }
    @Override
    public String toString() {
        return String.format("CS: %s  Sync: %s  RemoteVersion: %s", contentSetName, syncId, remoteVersion);
    }
    
}
