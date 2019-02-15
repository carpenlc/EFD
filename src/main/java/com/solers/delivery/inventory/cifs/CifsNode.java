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
package com.solers.delivery.inventory.cifs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.apache.log4j.Logger;

import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;

public class CifsNode extends AbstractNode {
    private static final Logger log = Logger.getLogger(CifsNode.class);
    
    private final CifsNode root;
    private final SmbFile backing;
    
    public CifsNode(SmbFile backing) {
        this(null, backing);
    }
    
    public CifsNode(CifsNode root, SmbFile backing) {
        this.root = root;
        this.backing = backing;
    }
    
    public CifsNode(URL url, NtlmPasswordAuthentication auth) {
        this(new SmbFile(url, auth));
    }

    @Override
    public List<Node> getChildren() {
        List<Node> l = new ArrayList<Node>();
        try {
            for (SmbFile s : backing.listFiles()) {
                l.add(new CifsNode(root, s));
            }
            return l;
        } catch (SmbException se) {
            log.warn("Failed to list children on CIFS.", se);
            return Collections.emptyList();
        }
    }

    @Override
    public String getName() {
        return stripTrailingSlash(backing.getName());
    }

    private String stripTrailingSlash(String original) {
        return original.endsWith("/") ?
            original.substring(0, original.length() - 1) :
            original;
    }
    
    @Override
    public Node getChild(String name) {
        SmbFile sf = null;
        try {
            sf = new SmbFile(backing, name);
            if (sf.exists()) {
                return new CifsNode(root, sf);
            }
            sf = new SmbFile(backing, name + "/");
            if (sf.exists()) {
                return new CifsNode(root, sf);
            }
        } catch (Exception e) {
            log.warn("Exception while seeking CIFS child node.", e);
        }
        return null;
    }
    
    @Override
    public Node getParent() {
        if (root == null) return null;
        String currPath = this.getPath();
        String parent = currPath.substring(0, currPath.lastIndexOf("/") + 1);
        try {
            return new CifsNode(root, new SmbFile(root.backing, parent));
        } catch (Exception e) {
            log.warn("Exception occurred when seeking CIFS parent.", e);
        }
        return null;
    }

    @Override
    public String getPath() {
        if (root == null) {
            return getName();
        } else {
            return stripTrailingSlash(backing.getCanonicalPath().substring(root.backing.getCanonicalPath().length()));
        }
    }
    
    @Override
    public long getSize() {
        try {
            return backing.length();
        } catch (SmbException se) {
            log.warn("Error while trying to get size attribute.", se);
            return 0l;
        }
    }

    @Override
    public long getTimestamp() {
        try {
            return backing.lastModified();
        } catch (SmbException se) {
            log.warn("Failed to retrieve timestamp data.", se);
            return 0l;
        }
    }

    @Override
    public boolean isDirectory() {
        try {
            return backing.isDirectory();
        } catch (SmbException se) {
            log.warn("Failed to retrieve file attribute.", se);
            return backing.getPath().endsWith("/");
        }
    }

    @Override
    public InputStream openStream() throws IOException {
        if (isDirectory()) return null;
        return backing.getInputStream();
    }
}
