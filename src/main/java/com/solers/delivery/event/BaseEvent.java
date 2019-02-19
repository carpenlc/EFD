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

import java.util.Date;

import com.solers.lucene.Converter;
import com.solers.lucene.Index;
import com.solers.lucene.LuceneField;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public abstract class BaseEvent implements DeliveryEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -2537456362366212557L;
    protected long timestamp;
    
    protected BaseEvent() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * The lucene field is named "timeStamp" for backwards compatibility
     * @see com.solers.delivery.event.Event#getTimeStamp()
     */
    @LuceneField(converter=Converter.DATE, name="timeStamp")
    public Date getSearchTimestamp() {
        return new Date(timestamp);
    }
    
    @LuceneField(index=Index.NO, converter=Converter.LONG, name="time")
    public long getTimestamp() {
        return timestamp;
    }
    
}
