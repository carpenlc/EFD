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
package com.solers.delivery.inventory.index.diff;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import com.solers.delivery.inventory.index.Packable;
import com.solers.util.HashFunction;

public final class HeaderStruct implements Packable {
    //only safe if kept private
    private static final byte[] MARKER = new byte[] {
        (byte) 0xD1,
        (byte) 0xFF
    };

    //expose for public inspection but don't rely on it.
    public static final byte[] DIFF_MARKER = Arrays.copyOf(MARKER, MARKER.length);
    
    /**
     * This is used to indicate the parent of the root directory, a virtual concept that
     * exists so we can extend the link-to-child logic and be consistent.
     */
    public static final String ROOT_PARENT = "*";
    public static final String SEPARATOR = "/";
    
    public final byte version;
    public final long lkg;
    public final String hash;
    public final int offset;
    
    private HeaderStruct(byte version, long lkg, String hash, int offset) {
        this.version = version;
        this.lkg = lkg;
        this.hash = hash;
        this.offset = offset;
    }
    
    public static HeaderStruct create(byte version, long lkg, String hash, int offset) {
        return new HeaderStruct(version, lkg, HashFunction.valueOf(hash).name(), offset);
    }
    
    public static HeaderStruct create(DataInput in) throws IOException {
        byte[] b = new byte[MARKER.length];
        for (int i = 0; i < b.length; ++i) {
            b[i] = in.readByte();
        }
        
        if (!Arrays.equals(b, MARKER)) return null;
        
        HeaderStruct hs = new HeaderStruct(
            in.readByte(),
            in.readLong(),
            in.readUTF(),
            in.readInt()
        );
        
        return hs;
    }
    
    @Override
    public byte[] pack() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            pack(dos);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException ioe) {
            return new byte[] {};
        } finally {
            IOUtils.closeQuietly(dos);
        }
    }

    @Override
    public void pack(DataOutput out) throws IOException {
        for (byte b : MARKER) {
            out.writeByte(b);
        }
        out.writeByte(version);
        out.writeLong(lkg);
        out.writeUTF(hash);
        out.writeInt(offset);
    }
}
