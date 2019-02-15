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
package com.solers.delivery.inventory.index;

import com.solers.util.HashFunction;

public final class HashUtil {
    private HashUtil() {
        
    }
    
    /**
     * Calculate the index of a particular string, using a specific hashing
     * algorithm, given that the receptacle has the specified number of
     * buckets. 
     * @param name the string to hash
     * @param hash the algorithm to use
     * @param buckets the number of receptacle buckets
     * @return the index of the target bucket
     */
    public static int index(String name, HashFunction hash, int buckets) {
        return Math.abs((int) hash.hash(name)) % buckets;
    }
    
    /**
     * Set a value into an array, moving through the array until a free
     * location is found.
     * @param table the array
     * @param desired the desired position in the array
     * @param value must be non-zero; the value to be set in the array
     * @return true if value is successfully placed into array
     */
    public static boolean set(int[] table, int desired, int value) {
        if (value == 0) throw new IllegalArgumentException("Value to set cannot be zero.");
        int start = desired;
        while (table[desired] != 0) {
            desired++;
            desired %= table.length;
            if (desired == start) return false;
        }
        table[desired] = value;
        return true;
    }
}
