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
package com.solers.delivery.transport.http.client.retry;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Required;

@Configurable("retryPolicy")
public class RetryPolicyLinearTime implements RetryPolicy {

    private static final Logger log = Logger.getLogger(RetryPolicyLinearTime.class);

    private static final int[] DELAY_TIMES = { 10, 30, 60, 90, 180, 300, 600, 900, 1800 };
    public static final TimeUnit DELAY_TIMES_UNIT = TimeUnit.SECONDS;

    private int maxTotalRetryTime;
    private TimeUnit maxTotalRetryTimeUnit;
    private int[] delayTimes = DELAY_TIMES;
    private TimeUnit delayTimesUnit = DELAY_TIMES_UNIT;
    private long totalRetryTime = 0;
    private int retryCount = 0;
    
    public RetryPolicyLinearTime() {
    }

    public RetryPolicyLinearTime(int maxTotalRetryTime, TimeUnit maxTotalRetryTimeUnit, int[] delayTimes, TimeUnit delayTimesUnit) {
        this.setMaxTotalRetryTime(maxTotalRetryTime);
        this.setMaxTotalRetryTimeUnit(maxTotalRetryTimeUnit);
        this.delayTimes = Arrays.copyOf(delayTimes, delayTimes.length);
        this.delayTimesUnit = delayTimesUnit;
        init();
    }
    
    public void init() {
        for (int time : delayTimes) {
            if (time <= 0) {
                throw new IllegalArgumentException("Delay time(s) must be positive integer values greater than zero");
            }
        }
    }

    @Required
    public void setMaxTotalRetryTime(int maxTotalRetryTime) {
        this.maxTotalRetryTime = maxTotalRetryTime;
    }

    @Required
    public void setMaxTotalRetryTimeUnit(TimeUnit maxTotalRetryTimeUnit) {
        this.maxTotalRetryTimeUnit = maxTotalRetryTimeUnit;
    }

    @Override
    public void disable() {
        this.totalRetryTime = delayTimesUnit.convert(maxTotalRetryTime, maxTotalRetryTimeUnit) + 1;
    }

    @Override
    public boolean isEnabled() {
        return (totalRetryTime <= delayTimesUnit.convert(maxTotalRetryTime, maxTotalRetryTimeUnit));
    }

    @Override
    public void execute() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            this.disable();
        } else if (this.isEnabled()) {
            retryCount++;
            int time = getTimeToSleep();
            totalRetryTime += time;
            log.error(String.format("Executing retry policy Retry#: %s  Sleeping for %s %s", retryCount, time, delayTimesUnit));
            try {
                delayTimesUnit.sleep(time);
            } catch (InterruptedException e) {
                disable();
                throw e;
            }
        }
    }

    public int getRetryCount() {
        return retryCount;
    }

    private int getTimeToSleep() {

        int time;
        if (retryCount > delayTimes.length) {
            time = delayTimes[delayTimes.length - 1];
        } else {
            time = delayTimes[retryCount - 1];
        }
        return time;
    }
}
