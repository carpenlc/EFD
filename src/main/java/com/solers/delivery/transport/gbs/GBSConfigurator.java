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
package com.solers.delivery.transport.gbs;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.listener.ListenerFactory;

import com.solers.delivery.security.PasswordType;
import com.solers.delivery.transport.gbs.ftp.FTPUserManager;
import com.solers.security.password.PasswordProvider;

public class GBSConfigurator {
    private final Properties config;
    private final PasswordProvider passwordManager;
    private static boolean enabled;
    
    public GBSConfigurator(Properties props, PasswordProvider passwordManager) {
        this.config = props;
        this.passwordManager = passwordManager;
    }
    
    public ListenerFactory getListenerFactory() {      
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(getInt("connection.port"));
        try {
            listenerFactory.setServerAddress(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            throw new RuntimeException("Localhost not available", ex);
        }
        return listenerFactory;
    }
    
    public ConnectionConfigFactory getConnectionFactory() {
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(getBoolean("connection-config.anonymousLoginEnabled"));
        return connectionConfigFactory;
    }
 
    public FTPUserManager getUserManager() {
        FTPUserManager userManager = new FTPUserManager();
        userManager.setName(getString("user.name"));
        userManager.setPassword(passwordManager.getPassword(PasswordType.FTP.key()));
        userManager.setDir(getString("user.dir"));
        userManager.setArchive(getString("user.archive"));
        userManager.configure();
        return userManager;        
    }
    
    public DataConnectionConfigurationFactory getDataConfigFactory() {
        DataConnectionConfigurationFactory dataConfigFactory = new DataConnectionConfigurationFactory();
        dataConfigFactory.setIdleTime(getInt("data-connection.idle-time"));
        dataConfigFactory.setPassivePorts(getString("data-connection.passive.ports"));
        dataConfigFactory.setActiveLocalPort(getInt("data-connection.active.local-port"));
        dataConfigFactory.setImplicitSsl(getBoolean("data-connection.implicitSsl"));
        return dataConfigFactory;
    }
    
    public boolean useImplicitSsl() {      
        return getBoolean("listener.implicitSsl");
    }
    
    private boolean getBoolean(String name) {
        return Boolean.parseBoolean(config.getProperty(name));
    }
    
    private int getInt(String name) {
        return Integer.parseInt(config.getProperty(name));
    }
    
    private String getString(String name) {
        return config.getProperty(name);
    }
    
    public static boolean isGBSEnabled() {
        return enabled;    
    }
    
    public static void setGBSEnabled(boolean enable) {
        enabled = enable;
    }
}