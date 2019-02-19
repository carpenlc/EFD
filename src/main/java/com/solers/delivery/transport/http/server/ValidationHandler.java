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

import static com.solers.delivery.transport.http.HTTPStatusCodes.ACCESS_DENIED;
import static com.solers.delivery.transport.http.HTTPStatusCodes.DISABLED_CONTENT_SET;
import static com.solers.delivery.transport.http.HTTPStatusCodes.NOT_FILE;
import static com.solers.delivery.transport.http.HTTPStatusCodes.NOT_FOUND;
import static com.solers.delivery.transport.http.HTTPStatusCodes.NOT_READABLE;
import static com.solers.delivery.transport.http.HTTPStatusCodes.NO_CONTENT_SET_NAME;
import static com.solers.delivery.transport.http.HTTPStatusCodes.UNAVAILABLE;
import static com.solers.delivery.transport.http.HTTPStatusCodes.UNKNOWN_CONTENT_SET_NAME;
import static com.solers.delivery.transport.http.HTTPStatusCodes.UNSUPPORTED_METHOD;

import java.io.File;
import java.io.IOException;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

//import org.bouncycastle.asn1.x509.X509Name;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import com.solers.delivery.content.supplier.DisabledContentSetException;
import com.solers.delivery.content.supplier.InventoryNotAvailableException;
import com.solers.delivery.content.supplier.InventoryNotChangedException;
import com.solers.delivery.content.supplier.UnregisteredContentSetException;
import com.solers.delivery.transport.http.HTTPHeaders;
import com.solers.delivery.transport.http.HTTPStatusCodes;

public class ValidationHandler extends BaseHandler {

    private static final String JAVAX_SERVLET_REQUEST_X509_CERTIFICATE = "javax.servlet.request.X509Certificate";
    private static Logger log = Logger.getLogger(ValidationHandler.class.getName());

    public void handle(String arg0, HttpServletRequest request, HttpServletResponse response, int arg3) throws IOException, ServletException {

        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            setRequestHandled(request, response);
            return;
        }
        
        if (request.getMethod().equalsIgnoreCase("PUT")) {
            // Do nothing pass through to EventHandler.
            return;
        }
        if (!request.getMethod().equalsIgnoreCase("GET")) {
            response.sendError(UNSUPPORTED_METHOD.code(), UNSUPPORTED_METHOD.message());
            return;
        }

        String contentSetName = this.getContentSetName(request);
        if (contentSetName == null || contentSetName.isEmpty()) {
            response.sendError(NO_CONTENT_SET_NAME.code(), NO_CONTENT_SET_NAME.message());
            setRequestHandled(request, response);
            return;
        }

        String path = getPathInfo(request);
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute(JAVAX_SERVLET_REQUEST_X509_CERTIFICATE);
        RDN RDNcommonName;
        String commonName = null;
        if ( certs != null && certs.length > 0 ) {
            X500Name x;
            try {
                x = new JcaX509CertificateHolder(certs[0]).getSubject();
                //X500Name x = new X500Name(certs[0].getSubjectX500Principal().getName());
                RDNcommonName = x.getRDNs(BCStyle.CN)[0];
                commonName = IETFUtils.valueToString(RDNcommonName.getFirst().getValue());
                //commonName = (String)x.getValues().get(x.getOIDs().indexOf(X500Name.CN));
            } catch (CertificateEncodingException ex) {
                log.error(String.format("Could not get JcaX509CertificateHolder for content set  %s", contentSetName));
                //java.util.logging.Logger.getLogger(ValidationHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        else {
            commonName = "No CN available";
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Request Path: %s  X509 Certificate Common Name: %s", path, commonName));
        }

        File file = null;
        try {
            if (cm.isAllowed(contentSetName, commonName)) {
                file = cm.getFile(contentSetName, path, getTimestamp(request));
            } else {
                log.error(String.format("Access denied for Content Set: %s using CN: %s", contentSetName, commonName));
                response.sendError(ACCESS_DENIED.code(), ACCESS_DENIED.message());
                setRequestHandled(request, response);
                return;
            }
        } catch (UnregisteredContentSetException e) {
            log.error(String.format("Content Set: %s is not registered with the Supplier", contentSetName));
            response.sendError(UNKNOWN_CONTENT_SET_NAME.code(), UNKNOWN_CONTENT_SET_NAME.message());
            setRequestHandled(request, response);
            return;
        } catch (DisabledContentSetException e) {
            log.error(String.format("Content Set: %s is currently disabled with the Supplier", contentSetName));
            response.sendError(DISABLED_CONTENT_SET.code(), DISABLED_CONTENT_SET.message());
            setRequestHandled(request, response);
            return;
        } catch (InventoryNotChangedException ince) {
            log.info(String.format("Content Set: %s received request for inventory that has not changed.", contentSetName));
            response.setStatus(HTTPStatusCodes.OK_NOT_MODIFIED.code());
            response.setHeader(HTTPHeaders.DATE.headerName(), new Date().toString());
            setRequestHandled(request, response);
            return;
        } catch (InventoryNotAvailableException inae) {
            log.info(String.format("Content Set: %s received request for inventory that has no bundles available.", contentSetName));
            response.sendError(UNAVAILABLE.code(), UNAVAILABLE.message());
            setRequestHandled(request, response);
            return;
        }

        // Test to see if the path is to a file that we can read
        if (file == null || !file.exists()) {
            response.sendError(NOT_FOUND.code(), NOT_FOUND.message());
            setRequestHandled(request, response);
            log.error(NOT_FOUND.message() + " : " + (file == null ? "null" : file.getAbsolutePath()));
            return;
        } else if (file.isDirectory()) {
            response.sendError(NOT_FILE.code(), NOT_FILE.message());
            setRequestHandled(request, response);
            log.error(NOT_FILE.message() + " : " + file.getAbsolutePath());
            return;
        } else if (!file.canRead()) {
            response.sendError(NOT_READABLE.code(), NOT_READABLE.message());
            setRequestHandled(request, response);
            log.error(NOT_READABLE.message() + " : " + file.getAbsolutePath());
            return;
        }
    }

    /**
     * @param request
     * @return
     */
    private long getTimestamp(HttpServletRequest request) {
        long timestamp = 0;
        String tsheader = request.getHeader(HTTPHeaders.INVENTORY_TIMESTAMP.headerName());
        try {
            if (tsheader != null)
                timestamp = Long.parseLong(tsheader);
        } catch (NumberFormatException nfe) {
            log.error("Could not parse timestamp from " + tsheader);
        }
        return timestamp;
    }
}
