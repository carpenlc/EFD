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
package com.solers.delivery.transport.http.client.connection;

import static com.solers.delivery.transport.http.HTTPHeaders.USER_AGENT;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;

import com.solers.security.ssl.TimeoutSocketFactory;

public class Connection {

    private static Logger log = Logger.getLogger(Connection.class);
    
    public static final int DEFAULT_TIMEOUT = 30 * 1000;
    private static final int MAX_REDIRECTS = 0;
    private static final int MAX_CONNECTIONS = 5;
    private static final int SSL_PORT = 443;
    private static final String SSL_PROTOCOL = "https";

    private final String host;
    private final int port;
    private final String localAddress;
    
    private HttpClient client;
    
    public Connection(String host, int port) {
        this.host = host;
        this.port = port;
        this.localAddress = "0.0.0.0";
    }
    
    public HttpClient getClient() {
        return client;
    }
    
    public void initialize() {
        client = new HttpClient(getHttpClientParams());
        client.setHttpConnectionManager(getConnectionManager());

        try {
            client.getHostConfiguration().setLocalAddress(InetAddress.getByName(localAddress));
        } catch (UnknownHostException e) {
            log.error("Problem with configured local address, using default (0.0.0.0)");
        }
        
        try {
            ProtocolSocketFactory factory = new ProtocolSocketFactoryWrapper(SSLContext.getDefault().getSocketFactory());
            client.getHostConfiguration().setHost(host, port, new Protocol(SSL_PROTOCOL, factory, SSL_PORT));
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException("SSL is not correctly configured", ex);
        }
    }
    
    private HttpClientParams getHttpClientParams() {
        HttpClientParams result = new HttpClientParams();
        result.setIntParameter(HttpClientParams.MAX_REDIRECTS, MAX_REDIRECTS);
        result.setParameter(HttpMethodParams.USER_AGENT, USER_AGENT.defaultValue());
        return result;
    }
    
    private HttpConnectionManager getConnectionManager() {
        HttpConnectionManagerParams parameters = new HttpConnectionManagerParams();
        parameters.setConnectionTimeout(DEFAULT_TIMEOUT);
        parameters.setSoTimeout(DEFAULT_TIMEOUT);
        parameters.setStaleCheckingEnabled(false);
        parameters.setMaxTotalConnections(MAX_CONNECTIONS);
        
        MultiThreadedHttpConnectionManager result = new MultiThreadedHttpConnectionManager();
        result.setParams(parameters);
        return result;
    }
    
    private static class ProtocolSocketFactoryWrapper implements ProtocolSocketFactory {
        private final SSLSocketFactory factory;

        private ProtocolSocketFactoryWrapper(SSLSocketFactory factory) {
            this.factory = factory;
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
            TimeoutSocketFactory f = (TimeoutSocketFactory) factory;
            return f.createSocket(host, port, localAddress, localPort, params.getConnectionTimeout());
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
            return factory.createSocket(host, port, localAddress, localPort);
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return factory.createSocket(host, port);
        }
    }
}
