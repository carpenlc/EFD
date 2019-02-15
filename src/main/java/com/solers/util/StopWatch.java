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

/**
 * StopWatch class. Unfortunately, commons.lang.StopWatch does not expose the
 * time that the stopwatch started, only the elapsed time
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class StopWatch {

    private long startTime = -1;
    private long endTime = -1;

    public StopWatch start() {
        startTime = System.currentTimeMillis();
        endTime = -1;
        return this;
    }

    public StopWatch stop() {
        endTime = System.currentTimeMillis();
        return this;
    }

    /**
     * @return If stop() has been called, return the time elapsed between start
     *         and stop. If stop has not been called, return the time elapsed
     *         since the stopwatch was started
     */
    public long getElapsedTime() {
        if (endTime == -1) {
            return System.currentTimeMillis() - getStartTime();
        }
        return endTime - getStartTime();
    }

    /**
     * @return The time the stopwatch was started
     * @throws IllegalStateException
     *             If the stopwatch has not been started
     */
    public long getStartTime() {
        if (startTime == -1) {
            throw new IllegalStateException("StopWatch has not been started");
        }
        return startTime;
    }

    public boolean isStarted() {
        return startTime > 0;
    }
}
