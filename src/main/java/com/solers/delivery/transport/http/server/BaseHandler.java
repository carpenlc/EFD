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
package com.solers.delivery.transport.http.server;

import static com.solers.delivery.transport.http.HTTPHeaders.CONTENT_SET_NAME;
import static com.solers.delivery.transport.http.HTTPHeaders.SYNC_ID;
import static com.solers.delivery.transport.http.HTTPHeaders.USER_AGENT;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.solers.delivery.content.supplier.ContentSetMapper;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public abstract class BaseHandler extends AbstractHandler {

    protected ContentSetMapper cm;

    public void setCm(ContentSetMapper cm) {
        this.cm = cm;
    }

    public String getContentSetName(HttpServletRequest request) {
        return request.getHeader(CONTENT_SET_NAME.headerName());
    }

    public String getSyncKey(HttpServletRequest request) {
        return request.getHeader(SYNC_ID.headerName());
    }

    /**
     * Set the Jetty request as handled.
     * 
     * @param request
     */
    protected void setRequestHandled(HttpServletRequest request, HttpServletResponse response) {
        Request base_request = null;

        if (request instanceof Request) {
            base_request = (Request) request;
        } else {
            base_request = HttpConnection.getCurrentConnection().getRequest();
        }
        response.addHeader(USER_AGENT.headerName(), USER_AGENT.defaultValue());
        base_request.setHandled(true);
    }

    /**
     * Get the path info without the leading "/"
     * 
     * @param request
     * @return
     */
    protected String getPathInfo(HttpServletRequest request) {
        String result = request.getPathInfo();
        if (result.startsWith("/") && !result.equals("/")) {
            result = result.substring(1);
        }
        return result;
    }
}
