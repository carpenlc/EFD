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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * This class contains utilities for manipulating file system paths,
 * such as finding all targets in a tree.
 */
public final class FileSystemUtil {
    private static final long RECURSE_YIELD_THRESHOLD = 100;
    private static final boolean YIELDING_ENABLED = true;
    private static final boolean SOFT_REFERENCES_ENABLED = true;
    
    private FileSystemUtil() {
        //do not instantiate
    }
    
    /**
     * @param file
     * @return True if {@code file} is readable
     */
    public static boolean isReadable(File file) {
        if (!file.canRead()) {
            return false;
        }
        // Double check, windows will return canRead -> true
        // even if you don't have read access
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (IOException ex) {
            return false;
        } finally {
            IOUtils.closeQuietly(is);
        }
        return true;
    }
    
    public static File getEFDHome() {
        String path = getEFDHomeProperty();
        return new File(FilenameUtils.normalize(path)).getAbsoluteFile();
    }
    
    /**
     * Returns new path that is based on efd.home
     * @param path - path to be appended to efd.home
     * @return ${efd.home}/${path}
     */
    public static File relativeToEFDHome(String path) {
        String efdProp = getEFDHomeProperty();
        //trim path
        path = (path == null ? "" : path.trim());
        
        //only fix path if not empty string
        if (path.length() > 0) {
            //remove ~ to ensure deterministic behavior of method
            //since calling getPrefixLength on ~user returns 5 because
            //getPrefix returns ~user/
            path = path.replaceAll("~", "");
            //get prefix length
            int prefixLength = FilenameUtils.getPrefixLength(path);
            //remove prefix if found
            //e.g. C:\foo becomes foo
            if (prefixLength > 0 && prefixLength <= path.length()) { 
                path = path.substring(prefixLength);
            }
        }
        
        String newPath = FilenameUtils.concat(efdProp, path);
        return new File(correctPath(FilenameUtils.normalize(newPath))).getAbsoluteFile();
    }
    
    /**
     * Break apart a path string into all valid file system nodes.
     * @param canonicalPath the canonical path to break down
     * @return an array of valid paths based on the parameter
     */
    public static String[] pathTree(String canonicalPath) {
        File f = new File(canonicalPath);
        List<String> paths = new ArrayList<String>();
        try {
            paths.add(f.getCanonicalPath());
            while ((f = f.getParentFile()) != null) {
                paths.add(f.getCanonicalPath());
            }
        } catch (IOException ioe) {
            return nonCanonicalizingPathTree(canonicalPath);
        }
        return paths.toArray(new String[paths.size()]);
    }
    
    /**
     * This is the previous pathTree method that relied on String manipulation.
     * @param canonicalPath canonical path to break down
     * @return array of valid paths based on the parameter
     */
    protected static String[] nonCanonicalizingPathTree(String canonicalPath) {
        Set<String> pathTargets = new HashSet<String>();
        String strip = canonicalPath;
        while (!"".equals(strip)) {
            if (strip.length() == 2 && strip.toCharArray()[1] == ':') {
                // check for windows drive designation
                pathTargets.add(strip + File.separator);
            } else {
                pathTargets.add(strip);
            }
            int lastSeparator = strip.lastIndexOf(File.separator);
            if (lastSeparator > 0) {
                strip = strip.substring(0, lastSeparator);
            } else if (lastSeparator == 0) {
                pathTargets.add(strip.substring(0, 1));
                break;
            } else {
                break;
            }
        }
        return pathTargets.toArray(new String[pathTargets.size()]);
    }
    
    
    /**
     * Retrieves the corrected path
     * 
     * @param path -
     *            path to correct
     * @return - path in the correct form
     */
    public static String correctPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.equals("")) {
            return path;
        }
        path = FilenameUtils.separatorsToUnix(path);
        String correctedPath;
        //handle issue where user enters 'c:' instead of 'c:\' or 'c:/'.
        if (path.length() == 2 && path.endsWith(":")) {
            path += "\\";
        }
        
        path = new File(path).getAbsolutePath();
        
        //handle lower case drive letter prefixes on windows paths
        if (path.length() > 1 && path.charAt(1) == ':') {
            char driveLetter = path.charAt(0);
            StringBuilder sb = new StringBuilder(path);
            sb.setCharAt(0, Character.toUpperCase(driveLetter));
            path = sb.toString(); 
        }
        correctedPath = FilenameUtils.normalizeNoEndSeparator(path);
        return FilenameUtils.separatorsToUnix(correctedPath);
    }
    
    /**
     * @param childPath
     * @param path
     * @return True if {@code childPath} is part of {@code path}
     */
    public static boolean pathIsChild(String childPath, String path) {
        String[] parts = pathTree(childPath);
        for (String part : parts) {
            if (part.equals(path)) return true;
        }
        return false;
    }
       
    /**
     * Check if a filename is invalid (e.g. whether it contains invalid characters).  
     * If the path contains any invalid characters, getCanonicalPath() will throw IOException.
     * @param path
     * @return True if {@code path} is a valid pathname
     */
    public static boolean isFilenameValid(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.getCanonicalPath();
            } catch (IOException ex) {
                return false;
            }
        }
        
        return true;
    }
 
    /**
     * Calculate statistics for a directory:
     * - Number of files
     * - Number of directories
     * - Size of files
     * @param directory directory to scan
     * @param soft - if true, use soft references to store child array
     * @return the stats, or in the case of error or interruption, null
     */
    public static Stats calculateDirectoryStatistics(File directory) {
         Stats stats = new Stats();
         try {
             if (!directory.exists()) return stats;
             calcDirStats(directory, stats, YIELDING_ENABLED, SOFT_REFERENCES_ENABLED);
         } catch (InterruptedException ie) {
             //stats will be invalid, so just return null
             return null;
         }
         return stats;
    }
    
    private static void calcDirStats(final File f, final Stats stats, final boolean yield, final boolean soft) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
        if (f.isFile()) {
            stats.incSize(f.length());
            stats.incFile();
        } else {
            stats.incDir();
            File[] children = f.listFiles();
            if (children == null) return;
            SoftReference<File[]> sr = null;
            if (soft) {
                sr = new SoftReference<File[]>(children);
            }
            
            /* KRJ 2016-10-25 
            
                Added null check for children in response to an
                HP Fortify finding
            */
            
            for (int pos = 0; pos < (children != null ? children.length : 0); ++pos) {
                File c = children[pos];
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                if (yield && stats.getItems() % RECURSE_YIELD_THRESHOLD == 0) {
                    Thread.sleep(1);        
                }
                
                if (c.isFile()) {
                    stats.incSize(c.length());
                    stats.incFile();
                } else {
                    if (soft) {
                        children = null;
                    }
                    
                    /* KRJ 2016-10-25
                    
                        Added null check of sr in the "if" block below, in 
                        response to an HP Fortify issue identified.
                    */
                    
                    calcDirStats(c, stats, yield, soft);
                    if (soft && (sr != null)) {
                        children = sr.get();
                        if (children == null) {
                            children = f.listFiles();
                            if (children == null) throw new InterruptedException("Did not expect directory to become null.");
                            stats.incCollections();
                            stats.incCollectedSize(children.length);
                            if (pos >= children.length) return;
                            sr = new SoftReference<File[]>(children);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns efd.home property
     * @return value assigned to efd.home property
     * @throws IllegalStateException if efd.home is not set
     */
    private static String getEFDHomeProperty() {
        String path = System.getProperty("efd.home");
        if (path == null) {
            throw new IllegalStateException("efd.home has not been set");
        }
        return path;
    }
    
    public static final class Stats {
        private long size = 0;
        private long dirs = 0;
        private long files = 0;
        private long collections = 0;
        private long collsize = 0;
        
        public long getSize() {
            return size;
        }
        
        public long getItems() {
            return dirs + files;
        }
        
        public long getFiles() {
            return files;
        }
        
        public long getDirs() {
            return dirs;
        }
        
        public long getCollections() {
            return collections;
        }
        
        void incSize(long inc) {
            size += inc;
        }
        
        void incFile() {
            files++;
        }
        
        void incDir() {
            dirs++;
        }
        
        void incCollections() {
            collections++;
        }
        
        void incCollectedSize(long amt) {
            collsize += amt;
        }
        
        public String toString() {
            return String.format("%d bytes in %d (%df/%dd) items, %d collections (%d refs, %d estimated collected size)", size, getItems(), files, dirs, collections, collsize, (collsize * 8 * 40) + (collections * 8));
        }
    }
}
