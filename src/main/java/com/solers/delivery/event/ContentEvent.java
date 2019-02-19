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

import java.util.HashMap;
import java.util.Map;

import com.solers.lucene.Index;
import com.solers.lucene.LuceneField;
import com.solers.lucene.PathAnalyzer;

public class ContentEvent extends BaseEvent {

    // This can be either a supplier providing a file,
    // a consumer pulling a file or
    // a consumer deleting a local file.

    /**
     * 
     */
    private static final long serialVersionUID = 4524048901101099421L;
    private final long contentSetId;
    private long bytesRequested;
    private long bytesManipulated;
    
    private ContentEventAction action;
    private ContentEventResult result;
    private String path;
    private String contentSetName;
    private String synchronizationId;
    private transient String message;
    private boolean directory;
    private long pathMtime;
    private long currentSize;
    private String hostname;

    public ContentEvent(long contentSetId) {
        this.contentSetId = contentSetId;
    }
    
    public boolean isSuccessful() {
        return result == null ? false : result.isSuccess();
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
    
    public long getContentSetId() {
        return contentSetId;
    }

    public String getSynchronizationId() {
        return synchronizationId;
    }

    public void setSynchronizationId(String synchronizationId) {
        this.synchronizationId = synchronizationId;
    }

    @LuceneField(index=Index.NO)
    public long getBytesManipulated() {
        return bytesManipulated;
    }
    
    @LuceneField(index=Index.NO)
    public long getBytesRequested() {
        return bytesRequested;
    }
    
    public void setBytesRequested(long bytesRequested) {
        this.bytesRequested = bytesRequested;
    }

    public void setBytesManipulated(long bytesTransfered) {
        this.bytesManipulated = bytesTransfered;
    }
    
    @LuceneField(index=Index.NO)
    public long getBytesFailed() {
        return getBytesRequested() - getBytesManipulated();
    }

    @LuceneField(index=Index.TOKENIZED)
    public String getAction() {
        return action.value;
    }
    
    public ContentEventAction getActionValue() {
        return action;
    }

    public void setAction(ContentEventAction action) {
        this.action = action;
    }
    
    public void setAction(String value) {
        setAction(ContentEventAction.valueOf(value));
    }
    public ContentEventResult getResultValue() {
        return result;
    }

    @LuceneField(index=Index.NO)
    public String getResult() {
        return result.value;
    }

    public void setResult(ContentEventResult result) {
        this.result = result;
    }
    
    public void setResult(String result) {
        setResult(ContentEventResult.valueOf(result));
    }

    @LuceneField(index=Index.TOKENIZED, analyzer=PathAnalyzer.class)
    public String getPath() {
        return path;
    }
    
    @LuceneField
    public String getPathKey() {
        return String.valueOf(getPath().hashCode());
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    @LuceneField(index=Index.NO)
    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public long getPathMtime() {
        return pathMtime;
    }

    public void setPathMtime(long pathMtime) {
        this.pathMtime = pathMtime;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public String getContentSetName() {
        return contentSetName;
    }

    public void setContentSetName(String contentSetName) {
        this.contentSetName = contentSetName;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String toString() {
        StringBuilder result =  new StringBuilder()
            .append("Action: <")
            .append(this.action)
            .append("> Content Set: <")
            .append(getContentSetName())
            .append("> Hostname: <")
            .append(getHostname())
            .append("> Path: <")
            .append(getPath())
            .append("> Directory: <")
            .append(isDirectory())
            .append("> Mtime: <")
            .append(getPathMtime());
        if (this.action == ContentEventAction.UPDATE) {
            result.append("> Current File Size: <").append(currentSize);
        }
        result.append("> Requested File Size: <")     
            .append(bytesRequested)
            .append("> Bytes Manipulated: <")
            .append(bytesManipulated)
            .append("> Bytes Failed: <")
            .append(getBytesFailed())
            .append("> Result: <")
            .append(this.result)
            .append("> Synchronization: <")
            .append(synchronizationId)
            .append(">");
        return result.toString();
    }

    public enum ContentEventAction {

        ADD("Add"), UPDATE("Update"), DELETE("Delete"), SUPPLY("Supply"), NONE("None");

        final String value;

        ContentEventAction(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
    
    public enum ContentEventResult {
        SUCCESS("Success", true, true), 
        REQUEST_FAILED("Request Failed", true, false), 
        TRANSFER_FAILED("Transfer Failed", true, false), 
        DELETE_FAILED("Delete Failed", true, false), 
        SENT_GBS("Sent via GBS", true, true), 
        GBS_ALREADY_RECEIVED("Already received via GBS", true, true),
        PENDING_GBS("Pending GBS", false, true),
        PENDING_DELETE("Pending Delete", false, true);

        final String value;
        private final boolean complete;
        private final boolean success;
        private static final Map<String, ContentEventResult> labelMap = new HashMap<String, ContentEventResult>(ContentEventResult.values().length);
        
        ContentEventResult(String value, boolean complete, boolean success) {
            this.value = value;
            this.complete = complete;
            this.success = success;
        }
        
        public boolean isSuccess() {
            return success;
        }

        public String value() {
            return value;
        }
        
        public boolean isComplete() {
            return complete;
        }
        
        public static ContentEventResult hasLabel(String label) {
            ContentEventResult cer = labelMap.get(label);
            if (cer == null) {
                for (ContentEventResult c : ContentEventResult.values()) {
                    if (c.value().equals(label)) {
                        labelMap.put(label, c);
                        return c;
                    }
                }
            }
            return cer;
        }
    }
}
