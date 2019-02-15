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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class StreamCopier {
    
    private static final int BUFFER_SIZE = 8192;
    private static final Callback<Integer> EMPTY_CALLBACK = new Callback<Integer>() { public void call(Integer e) {}};
    
    private final InputStream input;
    private final OutputStream output;
    private final AtomicBoolean stopped;
    
    public StreamCopier(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
        this.stopped = new AtomicBoolean(false);
    }
    
    public long copy() throws IOException {
        return copy(EMPTY_CALLBACK);
    }
    
    public long copy(Callback<Integer> callback) throws IOException {
        long bytesCopied = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = input.read(buffer);
        while ((-1 != bytesRead) && !stopped()) {
            output.write(buffer, 0, bytesRead);
            bytesCopied += bytesRead;
            callback.call(bytesRead);
            bytesRead = input.read(buffer);
        }
        return bytesCopied;
    }
    
    public void stop() {
        this.stopped.set(true);
    }
    
    private boolean stopped() {
        if (Thread.currentThread().isInterrupted()) {
            return true;
        }
        return this.stopped.get();
    }
}
