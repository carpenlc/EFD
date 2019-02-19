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
package com.solers.delivery.util;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public final class UUID {

    private static final int RANDOM_BYTES = 10;
    
    private static final SecureRandom secureRandom;
    
    static {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Could not configured PRNG", ex);
        } catch (NoSuchProviderException ex) {
            throw new RuntimeException("Could not configured PRNG", ex);
        }
    }
    
    public static String newSyncKey() {
        byte[] raw = new byte[RANDOM_BYTES];
        
        synchronized(secureRandom) {
            secureRandom.nextBytes(raw);
        }
        
        return String.format("%d-%s", System.currentTimeMillis(), new String(Hex.encodeHex(raw)));
    }
}
