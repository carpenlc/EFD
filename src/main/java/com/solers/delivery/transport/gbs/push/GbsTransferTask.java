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
package com.solers.delivery.transport.gbs.push;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.solers.delivery.domain.FtpConnection;
import com.solers.delivery.transport.gbs.Archive;
import com.solers.delivery.transport.gbs.GbsException;
import com.solers.delivery.transport.gbs.GbsFile;
import com.solers.delivery.transport.gbs.TarArchive;
import com.solers.delivery.transport.gbs.Transport;
import com.solers.delivery.transport.gbs.TransportException;
import com.solers.delivery.transport.gbs.ftp.FTPTransport;
import com.solers.delivery.transport.gbs.TransportType;

/**
 * This abstract class will Archive the appropriate files and transport them to the appropriate SBM via FTPS or FTP
 * 
 * @author dthemistokleous
 * 
 */
public class GbsTransferTask implements Runnable {
    /* Logger */
    private static final Logger log = Logger.getLogger(GbsTransferTask.class);

    protected static final int TIMEOUT = 5000; // 5 seconds

    private final String consumerContentSetName;
    private final String syncKey;
    private final List<GbsFile> files;
    private final File archiveDirectory;
    protected final FtpConnection connection;

    private Transport transport;
    
    public GbsTransferTask(TransportType type, String consumerContentSetName, String syncKey, 
        List<GbsFile> files, FtpConnection connection, File archiveDirectory) {
        this.consumerContentSetName = consumerContentSetName;
        this.syncKey = syncKey;
        this.files = files;
        this.connection = connection;
        this.archiveDirectory = archiveDirectory;
        transport = getTransport(type, connection);
}

    public void run() {
        log.info("Starting to upload archive for sync: "+syncKey);
        Archive archive = null;
        try {
            archive = new TarArchive(archiveDirectory, System.currentTimeMillis()+"-"+syncKey);
            archive.addFilesToArchive(files, consumerContentSetName, syncKey);
            upload(archive);
            log.info("Successfully uploaded archive for sync: "+syncKey);
        } catch (IOException e) {
            log.error("Failed to create archive for sync: "+syncKey, e);
        } catch (GbsException e) {
            log.error("Failed to transfer archive for sync: "+syncKey, e);
        } finally {
            if (archive != null && !archive.getArchive().delete()) {
                log.warn("Failed to delete Archive file: " + archive.getArchive().getName());
            }
        }        
    }

    /**
     * Uploads the archive to the provided FTP Server
     * 
     * @param archive - archive to upload
     */
    protected void upload(Archive archive) throws GbsException {
        try {
           transport.init();
           transport.upload(archive.getArchive());
        } catch (TransportException e) {
            throw new GbsException(e.getMessage());
        }
    }
    
    private Transport getTransport(TransportType transportType, FtpConnection counnection) {
        return new FTPTransport(transportType, connection);
    }
}
