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

import static com.solers.delivery.transport.http.HTTPHeaders.CONTENT_SET_NAME;
import static com.solers.delivery.transport.http.HTTPHeaders.SYNC_ID;

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;

import com.solers.delivery.transport.http.HTTPContextPaths;
import com.solers.delivery.transport.http.client.util.Session;

public class ContentGetMethod extends GetMethod {

    private static Logger log = Logger.getLogger(ContentGetMethod.class.getName());

    private final Session session;
    private final String remotePath;

    public ContentGetMethod(Session session, String remotePath) {
        super();
        this.session = session;
        this.remotePath = remotePath;
        
        HttpMethodParams params = new HttpMethodParams();
        params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
        this.setParams(params);
    }

    @Override
    public int execute(HttpState state, HttpConnection conn) throws IOException {
        
        String version = session.getRemoteVersion();
        super.setPath(this.buildPath(version));
        
        addRequestHeader(SYNC_ID.headerName(), session.getSyncId());
        addRequestHeader(CONTENT_SET_NAME.headerName(), session.getContentSetName());
        return super.execute(state, conn);
    }
    
    String buildPath(String version) {
        String path = null;
        String pathPrefix = (version == null) ? "" : HTTPContextPaths.CONTENT_CONTEXT_PATH;
        try {
            if (remotePath.startsWith("/")) {
                path =  (pathPrefix + URIUtil.encodePath(remotePath.replace('\\', '/')));
            } else {
                path =  (pathPrefix + "/" + URIUtil.encodePath(remotePath.replace('\\', '/')));
            }
            return path;
        } catch (URIException e) {
            log.error("Problem URI encoding path: " + remotePath, e);
            return path;
        } 
    }
}
