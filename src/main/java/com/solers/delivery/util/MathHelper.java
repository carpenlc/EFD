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

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class MathHelper {
    
    private static final BigDecimal ONEHUNDRED = new BigDecimal(100);
    private static final MathContext ROUNDING = new MathContext(3);
    
    public static double percentComplete(long completed, long total) {
        BigDecimal t = new BigDecimal(total);
        BigDecimal c = new BigDecimal(completed);
        return percentComplete(c, t);
    }
    
    public static double percentComplete(BigDecimal completed, BigDecimal total) {
        if (BigDecimal.ZERO.equals(total)) {
            return 100.00d;
        }
        return completed.divide(total, ROUNDING).multiply(ONEHUNDRED, ROUNDING).doubleValue();
    }
    
    public static long remainingTime(long elapsedTime, long processed, long remaining) {
        if (elapsedTime == 0) {
            return -1;
        }
        BigDecimal e = new BigDecimal(elapsedTime);
        BigDecimal p = new BigDecimal(processed);
        BigDecimal r = new BigDecimal(remaining);
        BigDecimal throughput = p.divide(e, ROUNDING);
        
        if (throughput.equals(BigDecimal.ZERO)) {
            return -1;
        }
        
        return r.divide(throughput, ROUNDING).longValue();
    }
}
