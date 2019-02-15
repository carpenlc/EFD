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

import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

public class NamedThreadFactory implements ThreadFactory {

    private static final Logger log = Logger.getLogger(NamedThreadFactory.class.getName());
    private static final int DEFAULT_STACK_TRACE_OFFSET = 3;

    private String threadName;

    public NamedThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    public NamedThreadFactory() {
        this(DEFAULT_STACK_TRACE_OFFSET);
    }
    
    public NamedThreadFactory(int offset) {
     // Uses the calling threads stack trace to determine which class called this constructor
        // and uses it's class name as the thread name
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String className = stackTrace[offset].getClassName();
        try {
            this.threadName = className.substring(className.lastIndexOf('.') + 1);
        } catch (RuntimeException e) {
            this.threadName = null;
        }
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        if (threadName != null) {
            t.setName(threadName);
        }
        t.setUncaughtExceptionHandler(new UEHLogger());
        return t;
    }

    public String getThreadName() {
        return threadName;
    }

    private static class UEHLogger implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            log.error("Thread terminated with exception: " + t.getName(), e);
        }
    }
}
