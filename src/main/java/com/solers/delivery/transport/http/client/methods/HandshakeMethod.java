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

import java.io.IOException;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.OptionsMethod;

public class HandshakeMethod extends OptionsMethod {
    
    public HandshakeMethod() {
        super();
    }
    
    @Override
    public int execute(HttpState state, HttpConnection conn) throws IOException {
        
        return super.execute(state, conn);
    }
}
