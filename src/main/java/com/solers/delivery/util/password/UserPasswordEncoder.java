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
package com.solers.delivery.util.password;

import org.springframework.dao.DataAccessException;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.security.providers.encoding.ShaPasswordEncoder;

/**
 * Encapsulates the configuration of the Spring Security Password encoder in one place.
 * 
 * Password encoding happens in the course of the normal application as well
 * as during the installer.  Normally the Spring Security encoder would be configured through 
 * Spring but we don't use any Spring configs during the installer
 * 
 * Thus, it is necessary that the encoder be accessible without special configuration.
 * 
 * We use a constant salt value so any salt value passed is ignored
 * 
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class UserPasswordEncoder implements PasswordEncoder {

    private final ShaPasswordEncoder encoder;
    private final String salt;
    
    public UserPasswordEncoder() {
        encoder = new ShaPasswordEncoder();
        salt = "SALT";
    }
    
    public String encodePassword(String rawPass) {
        return encoder.encodePassword(rawPass, this.salt);
    }
    
    public boolean isPasswordValid(String encPass, String rawPass) throws DataAccessException {
        return encoder.isPasswordValid(encPass, rawPass, this.salt);
    }
    
    @Override
    public String encodePassword(String rawPass, Object salt) throws DataAccessException {
        return encodePassword(rawPass);
    }
    
    @Override
    public boolean isPasswordValid(String encPass, String rawPass, Object salt) throws DataAccessException {
        return isPasswordValid(encPass, rawPass);
    }

}
