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
package com.solers.delivery.transport.http;

import org.mortbay.jetty.Response;

public enum HTTPStatusCodes {

    OK(Response.SC_OK, "The request succeeded normally"), 
    OK_PARTIAL(Response.SC_PARTIAL_CONTENT, "The request succeeded normally"),
    OK_NOT_MODIFIED(Response.SC_NOT_MODIFIED, "The request succeeded; the resource has not been modified."),
    // Status code (412) indicating that the precondition given in one or more of the request-header fields evaluated to false when it was tested on the server.
    UNKNOWN_CONTENT_SET_NAME(Response.SC_PRECONDITION_FAILED, "The content set name provided does not map to an existing content set"), 
    DISABLED_CONTENT_SET(Response.SC_FORBIDDEN, "The content set name provided is currently disabled"),
    // Status code (202) indicating that a request was accepted for processing, but was not completed.
    GBS_DELIVERY(Response.SC_ACCEPTED, "The file has been queued for delivery via GBS"),
    UNSUPPORTED_METHOD(Response.SC_METHOD_NOT_ALLOWED, "The HTTP method received is not supported"), 
    NO_CONTENT_SET_NAME(Response.SC_BAD_REQUEST, "No content set name header has been provided"), 
    NOT_FOUND(Response.SC_NOT_FOUND, "The requested file could not be found"), 
    NOT_READABLE(Response.SC_FORBIDDEN, "The requested file is not readable"), 
    ACCESS_DENIED(Response.SC_UNAUTHORIZED, "You do not have permission to access the requested content set"),
    NOT_FILE(Response.SC_FORBIDDEN, "The requested file is a directory"),
    UNAVAILABLE(Response.SC_SERVICE_UNAVAILABLE, "The requested supplier is not ready (no inventory bundles available)"),
    UNKNOWN(-1, "Unknown response");

    private final int code;
    private final String message;

    HTTPStatusCodes(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
    
    public static HTTPStatusCodes fromCode(int code) {
        for (HTTPStatusCodes value : HTTPStatusCodes.values()) {
            if (value.code() == code) {
                return value;
            }
        }
        
        return UNKNOWN;
    }

}
