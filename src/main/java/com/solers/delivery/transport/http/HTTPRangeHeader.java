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

public class HTTPRangeHeader {

    private static final String UNIT = "bytes";

    // We are only supporting ranger header of the type bytes=500- meaning we are only providing a byte
    // offset and the end position is assumed to be the end of the content, this is allowable per HTTP 1.1
    // See http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html section 14.35.1. 
    // We are not responding to invalid ranges with response status of 416

    private long firstPos;

    public HTTPRangeHeader(long firstPos) {
        this.firstPos = firstPos;
    }

    public String toString() {
        return new StringBuffer(UNIT).append('=').append(firstPos).append('-').toString();
    }

    public static HTTPRangeHeader parse(String header) {
        // Parse out the first and last pos, make sure the unit is valid
        try {
            String startPos = header.substring(header.indexOf('=') + 1, header.indexOf('-'));
            long pos = Long.parseLong(startPos);
            return new HTTPRangeHeader(pos);
        } catch (Exception e) {
            return new HTTPRangeHeader(0);
        }
    }

    public long getFirstPos() {
        return firstPos;
    }

    public void setFirstPos(long firstPos) {
        this.firstPos = firstPos;
    }
}
