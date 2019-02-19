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
package com.solers.delivery.event;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class GBSUpdateCompleteEvent extends BaseEvent {
    
    /**
     * 
     */
    private static final long serialVersionUID = 4586659916237439278L;
    private final long contentSetId;
    private final String syncKey;
    
    public GBSUpdateCompleteEvent(long contentSetId, String syncKey) {
        this.contentSetId = contentSetId;
        this.syncKey = syncKey;
    }
    
    public long getContentSetId() {
        return contentSetId;
    }
    
    public String getSyncId() {
        return SynchronizationEvent.getId(syncKey, contentSetId);
    }
    
    public String toString() {
        return String.format("Gbs update complete for %s", getSyncId());
    }
}
