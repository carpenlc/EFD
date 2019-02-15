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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.util.Arrays;

/**
 * On Windows, the System.console() will not be available when run under the installer
 * but we still need the readPassword() functionality. 
 * 
 * This class was added to bridge the two console mechanisms.
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public interface IOConsole {

    IOConsole DEFAULT = System.console() == null ? new SystemIOConsole() : new ConsoleIOConsole();

    void flush();
    void print(String s);
    void println(String s);
    char[] readPassword(String prompt, boolean confirm);
    String readLine();
    String readLine(String prompt);
}

abstract class BaseConsole implements IOConsole {
    
    @Override
    public final char[] readPassword(String prompt, boolean confirm) {
        boolean invalid = true;
        char [] result = null;
        while (invalid) {
            char[] password = doReadPassword(String.format("Enter [%s] password> ", prompt));
            if (confirm) {
                char[] confirmation = doReadPassword(String.format("Enter [%s] password again> ", prompt));
                if (empty(password) || empty(confirmation)) {
                    println("Entries cannot be blank");
                } else if (Arrays.equals(password, confirmation)) {
                    result = Arrays.copyOf(password, password.length);
                    invalid = false;
                } else {
                    println("Entries don't match");
                }
            } else if (empty(password)) {
                println("Entry cannot be blank");
            } else {
                result = Arrays.copyOf(password, password.length);
                invalid = false;
            }
        }
        return result;
    }
    
    private boolean empty(char [] array) {
        return array == null || array.length == 0;
    }
    
    protected abstract char[] doReadPassword(String prompt);
    
}

class ConsoleIOConsole extends BaseConsole {

    
    public void flush() {
        System.console().flush();
    }
    
    public void print(String s) {
        System.console().writer().print(s);
    }

    public void println(String s) {
        System.console().writer().println(s);
    }

    public String readLine() {
        return System.console().readLine();
    }
    
    public String readLine(String prompt) {
        print(String.format("%s> ", prompt));
        flush();
        return readLine();
    }

    public char[] doReadPassword(String prompt) {
        return System.console().readPassword(prompt);
    }
}

class SystemIOConsole extends BaseConsole {

    private static final int BUFSIZE = 128;
    
    /**
     * A ref to the original System.out in case somebody (ant) overwrote it
     */
    private static final PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), BUFSIZE), true);
    
    public void flush() {
        ps.flush();
    }

    public void print(String s) {
        ps.print(s);
    }

    public void println(String s) {
        ps.println(s);
    }
    
    public String readLine(String prompt) {
        print(String.format("%s> ", prompt));
        flush();
        return readLine();
    }

    public String readLine() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            return in.readLine();
        } catch (IOException ex) {
            throw new RuntimeException("Error reading from stdin: " + ex.getMessage(), ex);
        }

    }

    /**
     * Implementation from <a href="http://java.sun.com/developer/technicalArticles/Security/pwordmask/">this article</a>
     * 
     * @see com.solers.util.IOConsole#readPassword(java.lang.String, java.lang.Object[])
     */
    public char[] doReadPassword(String prompt) {
        InputStream in = System.in;
        Masker masker = new Masker(prompt);
        Thread thread = new Thread(masker);
        thread.start();

        char[] lineBuffer = new char[BUFSIZE];
        char[] buf = new char[BUFSIZE];

        int room = buf.length;
        int offset = 0;

        try {
            loop: while (true) {
                int c = in.read();
                switch (c) {
                    case -1:
                    case '\n':
                        break loop;

                    case '\r':
                        int c2 = in.read();
                        if ((c2 != '\n') && (c2 != -1)) {
                            if (!(in instanceof PushbackInputStream)) {
                                in = new PushbackInputStream(in);
                            }
                            ((PushbackInputStream) in).unread(c2);
                        } else {
                            break loop;
                        }

                    default:
                        if (--room < 0) {
                            buf = new char[offset + BUFSIZE];
                            room = buf.length - offset - 1;
                            System.arraycopy(lineBuffer, 0, buf, 0, offset);
                            Arrays.fill(lineBuffer, ' ');
                            lineBuffer = buf;
                        }
                        buf[offset++] = (char) c;
                        break;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error reading from stdin");
        }
        masker.stopMasking();
        if (offset == 0) {
            return null;
        }
        char[] ret = new char[offset];
        System.arraycopy(buf, 0, ret, 0, offset);
        Arrays.fill(buf, ' ');
        return ret;
    }

    class Masker implements Runnable {
        private volatile boolean done;
        private char echochar = ' ';

        /**
         * @param prompt
         *            The prompt displayed to the user
         */
        public Masker(String prompt) {
            print(prompt);
        }

        /**
         * Begin masking until asked to stop.
         */
        public void run() {
            int priority = Thread.currentThread().getPriority();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            try {
                done = false;
                while (!done) {
                    print("\010" + echochar);
                    try {
                        // attempt masking at this rate
                        Thread.sleep(1);
                    } catch (InterruptedException iex) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            } finally { // restore the original priority
                Thread.currentThread().setPriority(priority);
            }
        }

        /**
         * Instruct the thread to stop masking.
         */
        public void stopMasking() {
            this.done = true;
            print("\010");
        }
    }
}
