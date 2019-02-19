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
package com.solers.delivery.transport.http.client.methods;

import static com.solers.delivery.transport.http.HTTPHeaders.ACCEPT_ENCODING;
import static com.solers.delivery.transport.http.HTTPHeaders.CONTENT_ENCODING;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;

import com.solers.delivery.transport.http.client.util.Session;

public class GZIPAwareGetMethod extends ContentGetMethod {

    public GZIPAwareGetMethod(Session session, String remotePath) {
        super(session, remotePath);
    }

    @Override
    public int execute(HttpState state, HttpConnection conn) throws IOException {

        addRequestHeader(ACCEPT_ENCODING.headerName(), ACCEPT_ENCODING.defaultValue());
        return super.execute(state, conn);
    }

    @Override
    protected void readResponseBody(HttpState state, HttpConnection conn) throws IOException {

        super.readResponseBody(state, conn);
        Header contentEncodingHeader = getResponseHeader(CONTENT_ENCODING.headerName());
        if (contentEncodingHeader != null && contentEncodingHeader.getValue().equalsIgnoreCase("gzip")) {
            try (GZIPInputStream gZIPInputStream = new GZIPInputStream(getResponseStream())) {
                    setResponseStream(gZIPInputStream);               
            } catch (IOException ex) {
                throw new IOException("Error with GZIP input stream: " + ex.getMessage(), ex);
            }
        }
    }
}