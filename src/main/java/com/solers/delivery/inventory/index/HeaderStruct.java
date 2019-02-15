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

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

/**
 * The Header contains the basic information for reading an index
 * file.  It contains an identifier block, a version number (for
 * future extensions), and a pointer to the root node (or next
 * block). 
 */
public final class HeaderStruct implements Packable {
    //only safe if kept private
    private static final byte[] MARKER = new byte[] {
        (byte) 0xEF,
        (byte) 0xD3
    };

    //don't rely on public copy
    public static final byte[] EFD_MARKER = Arrays.copyOf(MARKER, MARKER.length);
    public final byte version;
    public final int rootOffset;
    
    private HeaderStruct(byte version, int rootOffset) {
        this.version = version;
        this.rootOffset = rootOffset;
    }
    
    public static HeaderStruct create(byte version, int rootOffset) {
        return new HeaderStruct(version, rootOffset);
    }
    
    public static HeaderStruct create(DataInput in) throws IOException {
        byte[] b = new byte[MARKER.length];
        try {
            for (int i = 0; i < b.length; ++i) {
                b[i] = in.readByte();
            }
        } catch (EOFException ex) {
            return null;
        }
        
        if (!Arrays.equals(b, MARKER)) return null;
        
        return new HeaderStruct(
            in.readByte(),
            in.readInt()
        );
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
        out.writeInt(rootOffset);
    }
}
