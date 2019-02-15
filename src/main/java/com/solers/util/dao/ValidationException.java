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
package com.solers.util.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ValidationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final List<String> messages;
    
    public ValidationException() {
        this(new String[] {});
    }
    
    public ValidationException(String... messages) {
        this.messages = new ArrayList<String>();
        if (messages != null) {
            for (String m : messages) {
                this.messages.add(m);
            }
        }
    }
    
    public void addMessage(String message) {
        messages.add(message);
    }
    
    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }
    
    @Override
    public String getMessage() {
        return messages.toString();
    }
}
