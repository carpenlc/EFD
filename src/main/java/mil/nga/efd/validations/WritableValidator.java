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
package com.solers.delivery.domain.validations.validators;

import java.io.File;

import org.hibernate.validator.Validator;

import com.solers.delivery.domain.validations.Writable;

public class WritableValidator implements Validator<Writable> {
    @Override
    public void initialize(Writable arg) {
        
    }

    @Override
    public boolean isValid(Object arg) {
        if (arg == null) return false;
        String path = (String) arg;
        
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        } else {
            return file.canWrite();
        }
    }
}
