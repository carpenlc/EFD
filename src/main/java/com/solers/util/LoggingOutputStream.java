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

/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * An OutputStream that flushes out to a Logger.
 * <p>
 * 
 * Note that no data is written out to the Logger until the stream is flushed or closed.
 * <p>
 * 
 * Example:
 * 
 * <pre>
 * // make sure everything sent to System.err is logged
 * System.setErr(new PrintStream(new LoggingOutputStream(Category.getRoot(), Priority.WARN), true));
 * 
 * // make sure everything sent to System.out is also logged
 * System.setOut(new PrintStream(new LoggingOutputStream(Category.getRoot(), Priority.INFO), true));
 * </pre>
 * 
 * @author <a href="mailto://Jim.Moore@rocketmail.com">Jim Moore</a>
 * @see Logger
 */
public class LoggingOutputStream extends OutputStream {
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    /**
     * The default number of bytes in the buffer. =2048
     */
    private static final int DEFAULT_BUFFER_LENGTH = 2048;

    /**
     * The logger to write to.
     */
    private final Logger logger;

    /**
     * The level to use when writing to the Category.
     */
    private final Level level;
    
    /**
     * Used to maintain the contract of {@link #close()}.
     */
    private boolean closed;

    /**
     * The internal buffer where data is stored.
     */
    private byte[] buf;

    /**
     * The number of valid bytes in the buffer. This value is always in the range <tt>0</tt> through <tt>buf.length</tt>; elements <tt>buf[0]</tt>
     * through <tt>buf[count-1]</tt> contain valid byte data.
     */
    private int count;

    /**
     * Remembers the size of the buffer for speed.
     */
    private int bufLength;

    /**
     * Creates the LoggingOutputStream to flush to the given Category.
     * 
     * @param logger
     *            the Logger to write to
     * 
     * @param level
     *            the Level to use when writing to the Category
     * 
     * @exception IllegalArgumentException
     *                if cat == null or priority == null
     */
    public LoggingOutputStream(Logger logger, Level level) {
        if (logger == null) {
            throw new IllegalArgumentException("logger == null");
        }
        if (level == null) {
            throw new IllegalArgumentException("level == null");
        }

        this.level = level;
        this.logger = logger;
        this.bufLength = DEFAULT_BUFFER_LENGTH;
        this.buf = new byte[DEFAULT_BUFFER_LENGTH];
        this.count = 0;
        this.closed = false;
    }

    /**
     * Closes this output stream and releases any system resources associated with this stream. The general contract of <code>close</code> is that it closes
     * the output stream. A closed stream cannot perform output operations and cannot be reopened.
     */
    public void close() {
        flush();
        closed = true;
    }

    /**
     * Writes the specified byte to this output stream. The general contract for <code>write</code> is that one byte is written to the output stream. The byte
     * to be written is the eight low-order bits of the argument <code>b</code>. The 24 high-order bits of <code>b</code> are ignored.
     * 
     * @param b
     *            the <code>byte</code> to write
     * 
     * @exception IOException
     *                if an I/O error occurs. In particular, an <code>IOException</code> may be thrown if the output stream has been closed.
     */
    public void write(final int b) throws IOException {
        if (closed) {
            throw new IOException("The stream has been closed.");
        }

        // don't log nulls
        if (b == 0) {
            return;
        }

        // would this be writing past the buffer?
        if (count == bufLength) {
            // grow the buffer
            bufLength = bufLength + DEFAULT_BUFFER_LENGTH;
            buf = Arrays.copyOf(buf, bufLength);
        }

        buf[count++] = (byte) b;
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out. The general contract of <code>flush</code> is that calling it is an
     * indication that, if any bytes previously written have been buffered by the implementation of the output stream, such bytes should immediately be written
     * to their intended destination.
     */
    public void flush() {
        if (count == 0) {
            return;
        }

        // don't print out blank lines; flushing from PrintStream puts out these
        if (count == LINE_SEPARATOR.length()) {
            char first = (char) buf[0];
            char second = (char) buf[1];
            
            if ((count == 1 && first == LINE_SEPARATOR.charAt(0)) 
                || 
                (count == 2 && second == LINE_SEPARATOR.charAt(1))) {
                reset();
                return;
            }
        }

        logger.log(level, new String(buf, 0, count).trim());

        reset();
    }

    private void reset() {
        // not resetting the buffer -- assuming that if it grew that it
        // will likely grow similarly again
        count = 0;
    }
}

