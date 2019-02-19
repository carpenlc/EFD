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

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.impl.DefaultFtpServer;
import org.apache.ftpserver.impl.DefaultFtpServerContext;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.log4j.Logger;

import com.solers.delivery.transport.gbs.GBSConfigurator;
import com.solers.delivery.transport.gbs.GbsException;

/**
 * An Ftp Server for EFD. This server will handle the pushing of archives from
 * the RBM to this server
 */
public class FTPServerWrapper {
    
    private static final Logger log = Logger.getLogger(FTPServerWrapper.class);

    private final GBSConfigurator config;
    private final boolean secure;
    private FtpServer server = null;

    public FTPServerWrapper(GBSConfigurator config, boolean secure) {
        this.config = config;
        this.secure = secure;
    }

    /**
     * Starts the FtpServer
     * 
     * @throws Exception
     * @throws Exception
     */
    public void start() throws GbsException {
        if (GBSConfigurator.isGBSEnabled()) {
            log.debug("Starting FtpServer");
            server = new DefaultFtpServer(getServerContext());
            try {
                server.start();
            } catch (FtpException ex) {
                throw new GbsException(ex);
            }
            log.info("Successfully started FtpServer");
        } else {
            log.info("FtpServer is not enabled");
        }
    }
    
    public void stop() {
        if (GBSConfigurator.isGBSEnabled()) {
            server.stop();
        }
    }
    
    private FtpServerContext getServerContext() {
        ConnectionConfigFactory connectionConfigFactory = config.getConnectionFactory();
        FTPUserManager userManager = config.getUserManager();

        DataConnectionConfigurationFactory dataConfigFactory = config.getDataConfigFactory();
        
        SslConfiguration sslConfig = null;
        ListenerFactory listenerFactory = config.getListenerFactory();
        if (secure) {
           sslConfig = new FTPSSLConfiguration();
           listenerFactory.setImplicitSsl(config.useImplicitSsl());
           dataConfigFactory.setSslConfiguration(sslConfig);
        }
        
        listenerFactory.setDataConnectionConfiguration(dataConfigFactory.createDataConnectionConfiguration());
        listenerFactory.setSslConfiguration(sslConfig);
        log.info("Using SSL: "+ secure);
        
        DefaultFtpServerContext ctx = new CustomFtpContext();
        ctx.setListener("default", listenerFactory.createListener());
        ctx.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
        ctx.setUserManager(userManager);

        return ctx;
    }

    /**
     * Stops the FtpServer
     */
    public void destroy() {
        log.debug("Stopping FtpServer");
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Override DefaultFtpServerContext so that 
     * createDefaultUsers is not called
     * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
     */
    private static class CustomFtpContext extends DefaultFtpServerContext {

        private CustomFtpContext() {
            super();
        }

        @Override
        public void createDefaultUsers() {
        }
    }
}
