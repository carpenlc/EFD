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

import static org.apache.commons.io.FilenameUtils.getFullPath;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.solers.delivery.transport.http.client.TransferContent;
import com.solers.util.Callback;
import com.solers.util.StreamCopier;

public class ContentCopy {

    private static final Logger log = Logger.getLogger(ContentCopy.class);

    private static final int MIN_FILENAME_LENGTH = 3;
    
    public void createFile(InputStream in, TransferContent transfer) throws FailedStreamToFileException, InterruptedException {
        OutputStream out = null;
        File tempFile = null;
        
        try {
            tempFile = getTempFile(transfer);
            out = new FileOutputStream(tempFile, true);
        } catch (IOException ex) {
            transfer.setBytesTransferred(0);
            throw new FailedStreamToFileException("Unable to create local file: " + transfer.getLocalPath(), ex);
        }

        try {
            doCopy(in, out, transfer);
            
            try {
                out.close();
            } catch (IOException ex) {
                log.warn("Error closing stream for "+tempFile, ex);
            }

            File destFile = new File(transfer.getLocalPath());
            if (destFile.exists()) {
                if (destFile.isDirectory()) {
                    FileUtils.deleteDirectory(destFile);
                } else {
                    destFile.delete();
                }
            }
            
            // See: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6213298
            // This issue can be reproduced by replicating a content set with a large number (~4000) of files
            // with multiple consumer transfers
            if (!tempFile.renameTo(destFile)) {
                boolean renamed = false;
                for (int i=0; i < 20 && !renamed; i++) {
                    if (tempFile.renameTo(destFile)) {
                        renamed = true;
                    } else {
                        Thread.sleep(50);
                    }
                }
                if (!renamed) {
                    log.warn("Could not rename: "+tempFile+" to "+destFile+" Does destFile exist? "+destFile.exists()+ " Tempfile? "+tempFile.exists());
                }
            }
            transfer.setFile(null);
        } catch (IOException e) {
            transfer.setFile(tempFile);
            throw new FailedStreamToFileException("IO Exception while writing file", e);
        } catch (InterruptedException e) {
            log.info("Interrupted while copying transfer: "+transfer);
            IOUtils.closeQuietly(out);
            if (! tempFile.delete() ) {
                log.warn("Unable to delete temporary file: " + tempFile.getAbsolutePath());
            }
            throw e;
        } finally {
            // Don't attempt to close this if the thread is interrupted.
            // Otherwise, httpclient will attempt to read the remainder 
            // of the stream
            // TransferTask will take care of aborting the method and
            // cleaning up the socket
            if (!Thread.currentThread().isInterrupted()) {
                IOUtils.closeQuietly(in);
            }
            IOUtils.closeQuietly(out);
        }
    }
    
    private boolean resuming(TransferContent xfer) {
        return xfer.getFile() != null;
    }
    
    private File getTempFile(TransferContent xfer) throws IOException {
        if (resuming(xfer)) {
            if (xfer.getFile().exists()) {
                return xfer.getFile();
            }
        }
        String path = separatorsToUnix(xfer.getLocalPath());
        String name = getName(path);
        String dirName = getFullPath(path);
        
        File dir = new File(dirName);
        // make the destination directory if it does not exist - should only be for inventory files
        dir.mkdirs();

        // File.createTempFile() throws an IllegalArgumentException if the prefix argument contains fewer than three characters
        if (name.length() < MIN_FILENAME_LENGTH) {
            name += "temp";
        }
        return File.createTempFile(name, ".tmp", dir);
            
    }

    private void doCopy(InputStream in, OutputStream out, final TransferContent transfer) throws IOException, InterruptedException {
        StreamCopier copier = new StreamCopier(in, out);
        Callback<Integer> cb = new Callback<Integer>() {
            public void call(Integer copied) {
                transfer.addBytesTransferred(copied);
            }
        };
        copier.copy(cb);
        
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        
        if (transfer.getPercentComplete() < 100) {
            throw new IOException();
        }
    }

    public static class FailedStreamToFileException extends Exception {

        private static final long serialVersionUID = 1L;

        public FailedStreamToFileException(String message) {
            super(message);
        }

        public FailedStreamToFileException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
