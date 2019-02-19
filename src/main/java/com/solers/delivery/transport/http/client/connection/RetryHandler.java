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
package com.solers.delivery.transport.http.client.connection;

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpMethod;

public class RetryHandler extends DefaultHttpMethodRetryHandler {

    private static final int MILLISECONDS_PER_SECOND = 1000;

    private final int retryDelaySeconds;

    public RetryHandler(int retryCount, int retryDelaySeconds, boolean requestSentRetryEnabled) {
        super(retryCount, requestSentRetryEnabled);
        this.retryDelaySeconds = retryDelaySeconds;
    }

    @Override
    public boolean retryMethod(HttpMethod method, IOException exception, int executionCount) {
        try {
            Thread.sleep(this.retryDelaySeconds * MILLISECONDS_PER_SECOND);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return super.retryMethod(method, exception, executionCount);
    }
}
