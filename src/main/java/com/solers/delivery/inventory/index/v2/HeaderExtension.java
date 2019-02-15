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
package com.solers.delivery.inventory.index.v2;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.solers.delivery.inventory.index.Packable;

/**
 * Header extension for version 2 of the inventory system.
 * Adds the hashing algorithm used for speeding up random access
 * to nodes in the file as well as the LKG time stamp from when
 * the inventory was created.
 */
final class HeaderExtension implements Packable {
    public final String hashAlgorithm;
    public final long lkg_timestamp;
    
    private HeaderExtension(String hashAlgorithm, long lkg_timestamp) {
        this.hashAlgorithm = hashAlgorithm;
        this.lkg_timestamp = lkg_timestamp;
    }
    
    public static HeaderExtension create(String hashAlgorithm, long lkg_timestamp) {
        return new HeaderExtension(hashAlgorithm, lkg_timestamp);
    }
    
    public static HeaderExtension create(DataInput in) throws IOException {
        String hash = in.readUTF();
        long lkg = in.readLong();
        return new HeaderExtension(hash, lkg);
    }

    @Override
    public byte[] pack() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try {
            pack(out);
            return baos.toByteArray();
        } catch (IOException ioe) {
            return new byte[0];
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    public void pack(DataOutput out) throws IOException {
        out.writeUTF(hashAlgorithm);
        out.writeLong(lkg_timestamp);
    }
}
