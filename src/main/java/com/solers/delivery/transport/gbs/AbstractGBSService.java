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
package com.solers.delivery.transport.gbs;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import mil.nga.efd.domain.ContentSet;

/**
 * 
 * This abstract class implements the common functionality shared by both
 * Consumer and Supplier sides of GBS service
 */
public abstract class AbstractGBSService implements GBSService {
    private final Map<Long, ContentSet> contentSetMap = new HashMap<Long, ContentSet>();
    
    public void addContentSet(ContentSet contentSet) {
        if (isGbsContentSet(contentSet)) {
            contentSetMap.put(contentSet.getId(), contentSet);
        }
    }

    public void removeContentSet(Long id) {
        if (isGbsContentSet(id)) {
           stopContentSet(id);
           contentSetMap.remove(id);
        }
    }

    public void startContentSet(Long id) {
        if (isGbsContentSet(id)) {
           doStart(id);
        }
    }

    public void stopContentSet(Long id) {
        if (isGbsContentSet(id)) {
           doStop(id);
        }
    }

    private boolean isGbsContentSet(ContentSet cs) {
       return (cs != null && cs.isSupportsGbsTransport());
    }
    
    private boolean isGbsContentSet(Long id) {
        return isGbsContentSet(contentSetMap.get(id));
    }

    protected String getContentSetName(Long id) {
       return contentSetMap.get(id).getName();  
    }
    
    protected Map<Long, ContentSet> getContentSets() {
       return contentSetMap;    
    }
    
    protected ContentSet getContentSet(Long id) {
        return contentSetMap.get(id);
    }
    
    protected abstract void doStart(Long id);
    protected abstract void doStop(Long id);
    
    public void addFile(String consumerContentSet, 
            String syncKey, 
            String supplierContentSet, 
            String host, 
            File file, 
            String path) {
    }
}
