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
package com.solers.delivery.alerts;

import java.util.Collection;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.solers.delivery.domain.Alert;
import com.solers.delivery.domain.Alert.AlertType;
import com.solers.delivery.rest.RestfulService;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.auth.RestAuthentication;
import com.solers.delivery.rest.converter.AdminConverter;
import com.solers.util.Page;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RestfulAlertService extends RestfulService implements AlertService {
    
    private final AdminConverter converter;
    
    public RestfulAlertService(String host, int port, RestAuthentication auth, AdminConverter converter) {
        super(host, port, auth);
        this.converter = converter;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Page<Alert> list(AlertType type, int startIndex, int numRecords) {
        Reference uri = uri("admin/alerts/");
        uri.addQueryParameter("type", String.valueOf(type.ordinal()));
        uri.addQueryParameter("startIndex", String.valueOf(startIndex));
        uri.addQueryParameter("numRecords", String.valueOf(numRecords));
        Request request = new Request(Method.GET, uri);
        Response response = handle(request);
        Utils.checkForException(response);
        return (Page<Alert>) converter.convert(response.getEntity());
    }

    @Override
    public void remove(Long id) {
        Response response = delete("admin/alerts/", id);
        Utils.checkForException(response);
    }

    @Override
    public void save(Alert alert) {
        Response response = put(converter.to(MediaType.TEXT_XML, alert), "admin/alerts/");
        Utils.checkForException(response);
    }

    @Override
    public Alert get(Long id) {
        Response response = get("admin/alerts/", id);
        Utils.checkForException(response);
        return (Alert) converter.convert(response.getEntity());
    }

    @Override
    public void remove(Collection<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return;
        }
        Reference uri = uri("admin/alerts/");
        for (Long id : ids) {
            uri.addQueryParameter("ids", id.toString());
        }
        Request request = new Request(Method.DELETE, uri);
        Response response = handle(request);
        Utils.checkForException(response);
    }

}
