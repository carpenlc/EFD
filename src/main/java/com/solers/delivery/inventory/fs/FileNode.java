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
package com.solers.delivery.inventory.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;

/**
 * FileNode extends the Node abstract class. FileNode is used to represent a
 * file or directory on a file system as a Node object.
 * 
 * @author JGimourginas
 */
public class FileNode extends AbstractNode implements Serializable {

    private static final long serialVersionUID = 1l;

    private static final Logger log = Logger.getLogger(FileNode.class);
    private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled();

    // File object to represent as a Node
    private final File file;
    private final String normalizedThis;
    // File object pointing to the root of the Node structure of which this Node
    // is an element
    protected final File root;
    protected final String normalizedRoot;

    /**
     * @param fileNode
     *            File to represent as a Node
     * @param rootFileNode
     *            File that points to the root of the Node structure of which
     *            this Node is a sub element
     */
    public FileNode(File fileNode, File rootFileNode) {
        super(fileNode.length());
        this.file = fileNode;
        this.root = rootFileNode;
        
        //pre-cache normalized paths so we are not doing this over and over again
        this.normalizedThis = FilenameUtils.normalizeNoEndSeparator(this.file.getAbsolutePath());
        File parent = root.getParentFile();
        this.normalizedRoot = FilenameUtils.normalizeNoEndSeparator((parent != null) ? parent.getAbsolutePath() : root.getAbsolutePath());
    }
    
    /**
     * We only have the root node.
     * @param rootNode
     */
    public FileNode(File rootNode) {
        this(rootNode, rootNode);
    }
    /**
     * @see Node#getName()
     */
    public String getName() {
        return file.getName();
    }
    
    /**
     * @see Node#getTimestamp()
     */
    public long getTimestamp() {
        return file.lastModified();
    }
    
    /**
     * @see Node#isDirectory()
     */
    public boolean isDirectory() {
        return file.isDirectory();
    }

    /**
     * @see Node#getChildren()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Node> getChildren() {
        if (IS_DEBUG_ENABLED)
            log.debug("Getting children");
        
        File[] children = file.listFiles();
        if (children == null || children.length == 0) {
            if (IS_DEBUG_ENABLED)
                log.debug("Returning no children");
            return Collections.emptyList();
        }
        
        List<Node> arrayList = new ArrayList<>();
        
        for (File children1 : children) {
            FileNode fn = new FileNode((File) children1, root);
            arrayList.add(fn);
        }
        
        /*
         * This inner ArrayList overrides the get method to transform
         * the File objects to FileNodes.  This allows us to skip
         * iterating the array and save some CPU & memory resources.
         */
        
        return arrayList;
        
        //return new ArrayList(Arrays.asList(children)) {
         //   private static final long serialVersionUID = 1L;

         //   @Override
         //   public Object get(int index) {
         //       Object o = super.get(index);
         //       return new FileNode((File) o, root);
         //   }
        //};
    }
    
    /**
     * @see Node#getPath()
     */
    public String getPath() {
        if (root == null) {
            return file.getName();
        } else {
            //whack the absolute part of the path preceding the root out of the string
            return normalizedThis.substring(normalizedRoot.length() + (normalizedRoot.endsWith(File.separator) ? 0 : 1));
        }
    }
    
    public Node getParent() {
        if (getPath().indexOf(File.separator) > -1) {
            File f = file.getParentFile();
            return new FileNode(f, root);
        }
        return null;
    }
    
    @Override
    public InputStream openStream() {
        if (file.exists() && file.canRead()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException fnfe) {
                //we pre-tested so should never be here
            }
        }
        return null;
    }
}
