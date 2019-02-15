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
package com.solers.delivery.inventory.index.v1;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.solers.delivery.inventory.index.Packable;

/**
 * The Entry structure is used for saving filesystem entries into the index file.
 * This contains all the information we need for replicating a content set:
 * - Name
 * - Size
 * - Last modified time stamp
 * - Directory/file indicator
 * - Child directories and files
 * Child elements are saved as binary offsets into the index file.  Entries that
 * cannot have children (file elements) do not have this block.
 */
final class EntryStruct implements Packable {
    
    private static final int OFFSET_ARRAY_TERMINATOR = -1;

    private EntryStruct(String name, boolean dir, long size, long lastModified) {
        this.name = name;
        this.isDirectory = dir;
        this.size = size;
        this.lastModified = lastModified;
    }
    
    public final String name;
    public final long size;
    public final long lastModified;
    public final boolean isDirectory;
    public final List<Integer> children = new ArrayList<Integer>();
    
    public static EntryStruct create(File f) {
        boolean isDirectory = f.isDirectory();
        EntryStruct props = new EntryStruct(
            f.getName(),
            isDirectory,
            isDirectory ? 0 : f.length(),
            f.lastModified()
            );
        return props;
    }
    
    public static EntryStruct create(DataInput in) throws IOException {
        String name = in.readUTF();
        boolean dir = in.readBoolean();
        EntryStruct s = new EntryStruct(
            name,
            dir,
            dir ? 0 : in.readLong(),
            in.readLong()
        );
        if (s.isDirectory) {
            int next;
            while ((next = in.readInt()) > OFFSET_ARRAY_TERMINATOR) {
                s.children.add(next);
            }
        }
        return s;
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
        out.writeUTF(name);
        out.writeBoolean(isDirectory);
        if (!isDirectory) {
            out.writeLong(size);
        }
        out.writeLong(lastModified);
        if (isDirectory) {
            for (int l : children) {
                out.writeInt(l);
            }
            out.writeInt(OFFSET_ARRAY_TERMINATOR);
        }
    }
}
