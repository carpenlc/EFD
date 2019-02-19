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
package com.solers.delivery.content.consumer.difference;

import java.util.Calendar;

import com.solers.delivery.inventory.node.Node;

/**
 * ContentDifference is now a simple pojo for keeping a record of a difference.
 * 
 * @author JGimourginas
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ContentDifference {

    private String path = null;
    private long timestamp;
    private long size;
    private boolean isDirectory;
    private ContentDifferenceActions action = null;
    private Calendar timeAdded = null;
    private Long consumerContentSetId = null;

    /**
     * Default constructor
     */
    public ContentDifference() {

    }

    /**
     * Constructor
     * 
     * @param action
     */
    public ContentDifference(ContentDifferenceActions action) {
        this.action = action;
        this.timeAdded = Calendar.getInstance();
    }

    /**
     * Constructor.
     * 
     * @param node
     *            with values that will be used to create a ContentDifference
     * @param action
     *            type of difference being described
     * @param consumerContentSetId
     *            indicates the consumer associated with the difference
     */
    public ContentDifference(Node node, ContentDifferenceActions action, Long consumerContentSetId) {
        this.path = node.getPath();
        this.timestamp = node.getTimestamp();
        this.size = node.getSize();
        this.isDirectory = node.isDirectory();
        this.action = action;
        this.consumerContentSetId = consumerContentSetId;
        this.timeAdded = Calendar.getInstance();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public ContentDifferenceActions getAction() {
        return action;
    }

    public void setAction(ContentDifferenceActions action) {
        this.action = action;
    }

    public Calendar getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Calendar timeAdded) {
        this.timeAdded = timeAdded;
    }

    public Long getConsumerContentSetId() {
        return consumerContentSetId;
    }

    public void setConsumerContentSetId(Long consumerContentSetId) {
        this.consumerContentSetId = consumerContentSetId;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }
}
