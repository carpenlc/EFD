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

import static com.solers.delivery.transport.http.HTTPContextPaths.CONTENT_CONTEXT_PATH;
import static com.solers.delivery.transport.http.HTTPContextPaths.EVENTS_CONTEXT_PATH;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpURI;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.servlet.Dispatcher;

import com.solers.delivery.transport.http.HTTPHeaders;

public class LegacyHandler extends BaseHandler {

    private static final String EFD_VERSION_REGEX = "EFD/[0-9]+.[0-9]+.?[0-9]*";

    private static final String FORWARDED = "forwarded";

    private Dispatcher contentDispatch;
    private Dispatcher eventDispatch;

    public void setContentDispatch(Dispatcher transportDispatch) {
        this.contentDispatch = transportDispatch;
    }

    public void setEventDispatch(Dispatcher eventDispatch) {
        this.eventDispatch = eventDispatch;
    }

    public void handle(String arg0, HttpServletRequest request, HttpServletResponse response, int arg3) throws IOException, ServletException {
        
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            setRequestHandled(request, response);
            return;
        }

        if (!request.getHeader(HTTPHeaders.USER_AGENT.headerName()).matches(EFD_VERSION_REGEX) && request.getAttribute(FORWARDED) == null) {
            
            request.setAttribute(FORWARDED, FORWARDED);
            Request base_request = null;

            if (request instanceof Request) {
                base_request = (Request) request;
            } else {
                base_request = HttpConnection.getCurrentConnection().getRequest();
            }

            if (base_request.getMethod().equalsIgnoreCase("GET") || base_request.getMethod().equalsIgnoreCase("OPTIONS") ) {
                base_request.setContextPath(CONTENT_CONTEXT_PATH);
                base_request.setUri(new HttpURI(CONTENT_CONTEXT_PATH + base_request.getUri()));
                contentDispatch.forward(base_request, response);
            } else if (base_request.getMethod().equalsIgnoreCase("PUT")) {
                base_request.setContextPath(EVENTS_CONTEXT_PATH);
                base_request.setUri(new HttpURI(EVENTS_CONTEXT_PATH + base_request.getUri()));
                eventDispatch.forward(base_request, response);
            }
        }
    }
}
