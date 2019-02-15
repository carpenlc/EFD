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

import java.io.DataOutput;
import java.io.IOException;

/**
 * Similar to Serializable, a Packable object can be written into
 * a custom binary format.
 */
public interface Packable {
    /**
     * Pack this item into a byte array and return it.
     */
    byte[] pack();
    
    /**
     * Pack this item into the supplied DataOutput.
     * @param out the location to receive this packed item
     * @throws IOException error during packing
     */
    void pack(DataOutput out) throws IOException;
}
