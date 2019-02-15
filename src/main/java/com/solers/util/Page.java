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
package com.solers.util;

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class Page<T> implements Iterable<T> {
    
    private int count;
    private List<T> page;
    
    public Page(int count, List<T> page) {
        this.count = count;
        this.page = page;
    }
    
    public Page() {
        
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<T> getPage() {
        return page;
    }

    public void setPage(List<T> page) {
        this.page = page;
    }

    @Override
    public Iterator<T> iterator() {
        return page.iterator();
    }
    
}
