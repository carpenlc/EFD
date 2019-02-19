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
package com.solers.delivery.content.status;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public enum SynchronizationResult {
    
    FAILED_NO_CONNECTION(false, false, true, "A connection to the supplier could not be made"),
    FAILED_INVALID_SUPPLIER(false, false, true, "Supplier does not exist"),
    FAILED_SUPPLIER_IS_DISABLED(false, false, true, "Supplier is disabled"),
    FAILED_UNAUTHORIZED(false, false, true, "You do not have permission to access the given supplier"),
    FAILED(false, false, true, "The synchronization failed"),
    
    COMPLETE_NOT_MODIFIED(false, false, false, "The content set was in synchronization"),
    COMPLETE_MODIFIED(true, false, false, "The content set was synchronized"),
    
    COMPLETE_WITH_ERRORS(false, true, false, "The synchronization completed but some items failed"),
    // I can't think of a better name for this
    COMPLETE_WITH_MISCOUNT(false, true, false, "The supplier sent more items than originally requested"),
    
    INTERRUPTED(false, true, false, "The synchronization was interrupted and did not complete"),
    INCOMPLETE(false, true, false, "The synchronization did not complete");
    
    private final boolean modified;
    private final boolean warning;
    private final boolean failure;
    private final String message;
    
    private SynchronizationResult(boolean modified, boolean warning, boolean failure, String message) {
        this.modified = modified;
        this.warning = warning;
        this.failure = failure;
        this.message = message;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isWarning() {
        return warning;
    }

    public boolean isFailure() {
        return failure;
    }

    public String getMessage() {
        return message;
    }

    public String getOriginalLabel() {
        return super.toString();
    }    

    @Override
    public String toString() {
        return getMessage();
    }
}
