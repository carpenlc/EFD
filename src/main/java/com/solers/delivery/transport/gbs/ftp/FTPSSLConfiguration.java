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


import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.log4j.Logger;


/**
 * SSL configuration implementation of the embedded FTP server
 */
public class FTPSSLConfiguration implements SslConfiguration {
    private static final Logger log = Logger.getLogger(FTPSSLConfiguration.class);
    
    private ClientAuth clientAuthRequired = ClientAuth.NONE;
    private final SSLContext sslContext;
    
    public FTPSSLConfiguration() {     
        try {
            this.sslContext = SSLContext.getDefault();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Could not get default SSL Context", ex);
        }
        setClientAuthentication(String.valueOf(sslContext.getDefaultSSLParameters().getNeedClientAuth()));
    }

    /**
     * Set what client authentication level to use, supported
     * values are "yes" or "true" for required authentication, 
     * "want" for wanted authentication and "false" or "none" 
     * for no authentication. Defaults to "none".
     * @param clientAuthReqd The desired authentication level
     */
    private void setClientAuthentication(String clientAuthReqd) {
        if("true".equalsIgnoreCase(clientAuthReqd) 
                || "yes".equalsIgnoreCase(clientAuthReqd)) {
            this.clientAuthRequired = ClientAuth.NEED;
        } else if("want".equalsIgnoreCase(clientAuthReqd)) {
            this.clientAuthRequired = ClientAuth.WANT;
        } else {
            this.clientAuthRequired = ClientAuth.NONE;
        }
    }
    
    /**
     * Return the SSL context for this configuration
     * @return The {@link SSLContext}
     * @throws GeneralSecurityException
     */
    public SSLContext getSSLContext() throws GeneralSecurityException {
        return sslContext;
    }

    /**
     * Return the SSL context for this configuration given the specified protocol
     * @param protocol The protocol, SSL or TLS must be supported
     * @return The {@link SSLContext}
     * @throws GeneralSecurityException
     */
    public SSLContext getSSLContext(String protocol) throws GeneralSecurityException {
        log.error("FTP layer trys to change SSL protocol.");
        throw new GeneralSecurityException("Not implemented.");
    }
    
    /**
     * Returns the cipher suites that should be enabled for this connection.
     * Must return null if the default (as decided by the JVM) cipher suites
     * should be used.
     * @return An array of cipher suites, or null.
     */
    public String[] getEnabledCipherSuites() {
        return sslContext.getDefaultSSLParameters().getCipherSuites();
    }

    /**
     * Return the required client authentication setting
     * @return {@link ClientAuth#NEED} if client authentication is required, 
     *      {@link ClientAuth#WANT} is client authentication is wanted or
     *      {@link ClientAuth#NONE} if no client authentication is the be performed  
     */
    public ClientAuth getClientAuth() {
        return clientAuthRequired;
    }
}