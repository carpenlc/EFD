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
package com.solers.delivery.transport.http.client;

import static com.solers.delivery.transport.http.HTTPHeaders.USER_AGENT;
import static com.solers.delivery.transport.http.HTTPParameters.MGT_TOTAL_BYTES;
import static com.solers.delivery.transport.http.HTTPParameters.MGT_TOTAL_REQUESTS;
import static com.solers.delivery.transport.http.client.methods.ManagementMethod.Type.METRICS;
import static com.solers.delivery.transport.http.client.methods.ManagementMethod.Type.START;
import static com.solers.delivery.transport.http.client.methods.ManagementMethod.Type.STOP;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;

import com.solers.delivery.transport.http.client.TransferService.TransportUnavailableException;
import com.solers.delivery.transport.http.client.methods.ManagementMethod;
import com.solers.delivery.transport.http.client.methods.HandshakeMethod;
import com.solers.delivery.transport.http.client.util.Session;

/**
 * @author DBailey
 * 
 */
public class TransferMangement {

    private static final Logger log = Logger.getLogger(TransferMangement.class);

    private final HttpClient client;

    public TransferMangement(HttpClient client) {
        this.client = client;
    }

    public void sendStart(Session session) {
        eventRequest(new ManagementMethod(session, START));
    }

    public void sendStop(Session session) {
        eventRequest(new ManagementMethod(session, STOP));
    }
    
    public String sendHandshake() {
        HandshakeMethod pvm = new HandshakeMethod();
        eventRequest(pvm);
        String version;
        try {
            version = pvm.getResponseHeader(USER_AGENT.headerName()).getValue();
        } catch (NullPointerException e) {
            version = null;
        }
        return version;
    }

    public void sendMetrics(Session session, long totalRequests, long totalBytes) {
        ManagementMethod method = new ManagementMethod(session, METRICS);
        method.addParameters(new NameValuePair(MGT_TOTAL_REQUESTS.parameterName(), Long.toString(totalRequests)));
        method.addParameters(new NameValuePair(MGT_TOTAL_BYTES.parameterName(), Long.toString(totalBytes)));
        eventRequest(method);
    }

    private void eventRequest(HttpMethodBase method) throws TransportUnavailableException {
        try {
            new ManagementTask(client, method).call();
        } catch (Exception e) {
            log.error("Unexpected error occurred while executing management method", e);
            throw new TransportUnavailableException(e);
        }
    }

    private static final class ManagementTask implements Callable<Transfer> {

        private final HttpClient client;
        private final HttpMethodBase method;

        private ManagementTask(HttpClient client, HttpMethodBase method) {
            this.client = client;
            this.method = method;
        }

        @Override
        public Transfer call() throws Exception {
            try {
                client.executeMethod(method);
                method.releaseConnection();
            } catch (HttpException e) {
                log.error("Problem sending: " + method.toString(), e);
                throw new TransportUnavailableException(e);
            } catch (IOException e) {
                log.error("Problem sending: " + method.toString(), e);
                throw new TransportUnavailableException(e);
            }
            return null;
        }
    }
}
