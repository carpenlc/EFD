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
package com.solers.delivery.transport.gbs.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.log4j.Logger;

import com.solers.delivery.domain.FtpConnection;
import com.solers.delivery.transport.gbs.GbsException;
import com.solers.delivery.transport.gbs.Transport;
import com.solers.delivery.transport.gbs.TransportException;
import com.solers.delivery.transport.gbs.TransportType;

public class FTPTransport implements Transport {
    private static final Logger log = Logger.getLogger(FTPTransport.class);

    private FTPClient ftpclient = null;
    private FtpConnection connection;
    protected static final int TIMEOUT = 5000; // 5 seconds
    private TransportType transportType;

    public FTPTransport(TransportType type, FtpConnection connection) {
        this.transportType = type;
        this.connection = connection;
    }

    public void upload(File file) throws TransportException {
        try {
            ftpclient.connect(connection.getHost(), Integer.parseInt(connection.getPort()));
            int reply = ftpclient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                log.error("FTP server refused connection.");
                throw new GbsException("FTP server refused connection.");
            }

            if (!ftpclient.login(connection.getUsername(), connection.getPassword())) {
                log.error("Log in to FTP server failed.");
                throw new GbsException("Log in to FTP server failed.");
            }

            if (!ftpclient.changeWorkingDirectory(connection.getDirectory())) {
                log.error("Change working directory failed:" + connection.getDirectory());
                throw new GbsException("Change working directory failed.");
            }

            if (connection.isPassive()) {
                ftpclient.enterLocalPassiveMode();
            }
            
            if (transportType == TransportType.FTPS) {
                FTPSClient ftpsClient = (FTPSClient) ftpclient;
                ftpsClient.execPBSZ(0);
                ftpsClient.execPROT("P");
            }

            InputStream input = new FileInputStream(file);
            try {
                ftpclient.setFileType(FTP.BINARY_FILE_TYPE);
                if (!ftpclient.storeFile(file.getName(), input)) {
                    log.error("Store file operation failed.");
                    throw new GbsException("Store file operation failed.");
                }
            } finally {
                input.close();
            }
            
        } catch (IOException ex) {
            log.error("Error upload file: " + ex.getMessage());
            throw new GbsException("Failed to upload Archive: " + ex.getMessage(), ex);
        } finally {
            try {
                ftpclient.logout();
                ftpclient.disconnect();
            } catch (IOException ex) {
                throw new GbsException("Failed to disconnect ftp " + ex.getMessage(), ex);
            }
        }
    }

    public void init() throws TransportException {
        if (transportType == TransportType.FTPS) {
            log.info("Initializing FTPS Transfer Task");
            try {
                ftpclient = new FTPSClient(connection.isImplicit(), SSLContext.getDefault());
            } catch (NoSuchAlgorithmException e) {
                log.error("Cannot create ftps client: " + e.getMessage());
                throw new TransportException(e.getMessage());
            }
        } else {
            log.info("Initializing FTP Transfer Task");
            ftpclient = new FTPClient();
        }
        ftpclient.setDefaultTimeout(TIMEOUT);
        ftpclient.addProtocolCommandListener(new ProtocolCommandLogger());
    }
    
    private static class ProtocolCommandLogger implements ProtocolCommandListener {

        @Override
        public void protocolCommandSent(ProtocolCommandEvent event) {
            if (log.isDebugEnabled()) {
                log.debug("Command sent: "+event.getMessage().trim());
            }
        }

        @Override
        public void protocolReplyReceived(ProtocolCommandEvent event) {
            if (log.isDebugEnabled()) {
                log.debug("Reply received: "+event.getMessage().trim());
            }
        }
        
    }
}