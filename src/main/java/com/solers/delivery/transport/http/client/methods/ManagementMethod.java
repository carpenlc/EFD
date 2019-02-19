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
import static com.solers.delivery.transport.http.HTTPParameters.MGT_TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.solers.delivery.transport.http.HTTPContextPaths;
import com.solers.delivery.transport.http.client.util.Session;

public class ManagementMethod extends PutMethod {
    
    private static final int MAX_RETRIES = 4;

    private final Session session;
    private final Type type;
        
    protected List<NameValuePair> params;

    public ManagementMethod(Session session, Type type) {
        super();
        this.session = session;
        this.type = type;
        this.params = new ArrayList<NameValuePair>();
        String path = (session.getRemoteVersion() == null) ? "/" : HTTPContextPaths.EVENTS_CONTEXT_PATH + "/";
        setPath(path);
        
        HttpMethodParams params = new HttpMethodParams();
        params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(MAX_RETRIES, true));
        this.setParams(params);
    }
    
    public void addParameters(NameValuePair nvp) {
        this.params.add(nvp);
    }
    
    @Override
    public int execute(HttpState state, HttpConnection conn) throws IOException {
        
        addRequestHeader(SYNC_ID.headerName(), session.getSyncId());
        addRequestHeader(CONTENT_SET_NAME.headerName(), session.getContentSetName());
        
        this.addParameters(new NameValuePair(MGT_TYPE.parameterName(), type.toString()));
        setQueryString(params.toArray(new NameValuePair[0]));
        return super.execute(state, conn);
    }
    
    @Override
    public String toString() {
        return String.format("MgtMethod: %s  Session: %s ",type.toString(),session.toString());
    }
    
    public enum Type {
        START, STOP, METRICS
    };
}
