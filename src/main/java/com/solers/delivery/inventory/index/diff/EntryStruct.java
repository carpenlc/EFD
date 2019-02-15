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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.solers.delivery.inventory.index.Packable;
import com.solers.delivery.inventory.node.Node;

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
    
    private EntryStruct(boolean changed, String name, boolean dir, long size, long lastModified, boolean child) {
        this.changed = changed;
        this.name = name;
        this.isDirectory = dir;
        this.size = size;
        this.lastModified = lastModified;
        this.child = child;
    }
    
    public final boolean child;
    
    public final boolean changed;
    public final String name;
    public final boolean isDirectory;
    public final long lastModified;
    
    //only for leaf elements
    public final long size;
    
    //only for inner node elements
    public final List<Integer> children = new ArrayList<Integer>();
    
    public static EntryStruct create(Node f, boolean changed, boolean child) {
        boolean isDirectory = f.isDirectory();
        EntryStruct props = new EntryStruct(
            changed,
            f.getName(),
            isDirectory,
            isDirectory ? 0 : f.getSize(),
            f.getTimestamp(),
            child);
        return props;
    }
    
    public static EntryStruct create(DataInput in, boolean child) throws IOException {
        boolean changed = true;
        if (!child) changed = in.readBoolean();
        String name = in.readUTF();
        EntryStruct s;
        boolean dir = in.readBoolean();
        if (changed) {
            long timestamp = in.readLong();
            long size = dir ? 0 : in.readLong();
            s = new EntryStruct(changed, name, dir, size, timestamp, child);
            if (s.isDirectory) {
                int count = in.readInt();
                for (int i = 0; i < count; ++i) {
                    s.children.add(in.readInt());
                }
            }
        } else {
            s = new EntryStruct(changed, name, dir, 0, 0, child);
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
            return new byte[0];
        } finally {
            IOUtils.closeQuietly(dos);
        }
    }
    
    @Override
    public void pack(DataOutput out) throws IOException {
        if (!child) out.writeBoolean(changed);
        out.writeUTF(name);
        out.writeBoolean(isDirectory);
        //We only need to write the metadata for an ADD and UPDATE.  The 
        //name/path is the only necessary information for a removed node.
        if (changed) {
            out.writeLong(lastModified);
            if (isDirectory) {
                out.writeInt(children.size());
                for (int l : children) {
                    out.writeInt(l);
                }
            } else {
                out.writeLong(size);
            }
        }
    }
}
