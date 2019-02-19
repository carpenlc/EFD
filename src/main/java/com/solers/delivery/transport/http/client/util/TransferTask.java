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

import static com.solers.delivery.transport.http.client.Transfer.Status.FAILED_TRANSFER;

import java.util.concurrent.Callable;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.StatusLine;
import org.apache.log4j.Logger;

import com.solers.delivery.transport.http.HTTPStatusCodes;
import com.solers.delivery.transport.http.client.Transfer;
import com.solers.delivery.transport.http.client.TransferContent;
import com.solers.delivery.transport.http.client.retry.RetryPolicy;
import java.io.InputStream;

public class TransferTask implements Callable<Transfer> {

    private static final Logger log = Logger.getLogger(TransferTask.class);

    private final HttpClient client;
    private final Transfer transfer;
    private final ContentCopy copier;
    private final RetryPolicy policy;

    public TransferTask(HttpClient client, Transfer transfer, ContentCopy copier, RetryPolicy retryPolicy) {
        this.client = client;
        this.transfer = transfer;
        this.copier = copier;
        this.policy = retryPolicy;
    }

    public Transfer call() {

        transfer.setStatus(Transfer.Status.RUNNING);
        fetchData();
        while (policy.isEnabled()) {
            try {
                policy.execute();
                transfer.setStatus(Transfer.Status.RETRY);
                fetchData();
            } catch (InterruptedException e) {
                // No need to change the Transfer object as we want the original cause of failure not the interruption
                policy.disable();
                Thread.currentThread().interrupt();
            }
        }
        return transfer;
    }

    private void fetchData() {
        HttpMethod method = transfer.getHttpMethod();
        try {
            client.executeMethod(method);
            int statusCode = method.getStatusCode();
            if (log.isDebugEnabled()) {
                log.debug(String.format("Request executed, HTTP status code: %d   Transfer.toString(): %s", statusCode, transfer.toString()));
            }
            HTTPStatusCodes code = HTTPStatusCodes.fromCode(statusCode);
            
            if (shouldCopy(code)) {
                try (InputStream inputStream = method.getResponseBodyAsStream()) {
                    copier.createFile(inputStream, (TransferContent)transfer);
                }
            }
            
            if (disablePolicy(code)) {
                policy.disable();
            }
            
            if (isFailure(code)) {
                logFailure(method.getStatusLine());
            }
            
            if (shouldCleanup(code)) {
                transfer.cleanup();
            }
            transfer.setStatus(getStatus(code));
            
        } catch (InterruptedException e) {
            transfer.setStatusCleanup(FAILED_TRANSFER);
            logFailure(method.getStatusLine());
            policy.disable(); // Do not retry
            method.abort();
            Thread.currentThread().interrupt();
        } catch (Exception e) { // HTTPException & IOException from client.execute()
            transfer.setStatus(FAILED_TRANSFER);
            logFailure(method.getStatusLine());
            logException(e);
        } finally {
            method.releaseConnection();
        }
    }
    
    private boolean disablePolicy(HTTPStatusCodes code) {
        switch(code) {
            case UNKNOWN:
                return false;
            default:
                return true;
        }
    }
    
    private boolean shouldCleanup(HTTPStatusCodes code) {
        switch (code) {
            case UNKNOWN:
                return false;
            default:
                return true;
        }
    }
    
    private boolean shouldCopy(HTTPStatusCodes code) {
        return code == HTTPStatusCodes.OK || code == HTTPStatusCodes.OK_PARTIAL;
    }
    
    private boolean isFailure(HTTPStatusCodes code) {
        switch (code) {
            case GBS_DELIVERY:
            case OK_NOT_MODIFIED:
            case OK:
            case OK_PARTIAL:
                return false;
            default:
                return true;
        }
    }
    
    private Transfer.Status getStatus(HTTPStatusCodes code) {
        switch (code) {
            case OK:
            case OK_PARTIAL:
                return Transfer.Status.COMPLETED;
            case GBS_DELIVERY:
                return Transfer.Status.PENDING_GBS;
            case OK_NOT_MODIFIED:
                return Transfer.Status.COMPLETE_NOT_MODIFIED;
            case DISABLED_CONTENT_SET:
                return Transfer.Status.FAILED_SUPPLIER_IS_DISABLED;
            case NO_CONTENT_SET_NAME:
                return Transfer.Status.FAILED_INVALID_SUPPLIER;
            case ACCESS_DENIED:
                return Transfer.Status.FAILED_UNAUTHORIZED;
            default:
                return Transfer.Status.FAILED_TRANSFER;
        }
    }

    private void logFailure(StatusLine status) {
        String statusMsg = (status == null) ? "No HTTP Status Line" : status.toString();
        String host = client.getHostConfiguration().getHost();
        int port = client.getHostConfiguration().getPort();
        log.error(String.format("Transfer failed: %s  from: %s:%d  status: %s", transfer.getLocalPath(), host, port, statusMsg));
    }

    private void logException(Exception e) {
        Throwable t = e;
        String rootMessage = e.getMessage();
        while ((t = t.getCause()) != null) {
            if (t.getCause() == null)
                break;
            rootMessage = t.getMessage();
        }
        log.error(String.format("Transfer exception  Root Message: %s  Full error message: %s", rootMessage, e.getMessage()), e);
    }
}
