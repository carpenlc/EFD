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
package com.solers.delivery.transport.gbs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import org.apache.commons.compress.tar.archivers.TarArchiveEntry;
import org.apache.commons.compress.tar.archivers.TarArchiveInputStream;
import org.apache.commons.compress.tar.archivers.TarArchiveOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solers.delivery.transport.gbs.poll.ExtractProgress;

import mil.nga.efd.controllers.AlertDAOHibernate;

import com.solers.delivery.event.ContentEvent.ContentEventAction;

public class TarArchive implements Archive {
	
	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		TarArchive.class);

    private static final int BUFFER_SIZE = 1024;
    private static final String CONSUMER_CONTENT_SET = "ConsumerContentSet";
    private static final String SYNC_KEY = "SyncKey";

    private final File tarFile;
    private final Properties properties;

    private List<String> fileNames;
    
    //Total size of the archive excluding the property file
    private long archiveSize;
    
    public TarArchive(File baseDirectory, String archiveName) throws IOException {
        this(new File(baseDirectory, archiveName + Archive.TAR_EXTENSION));
    }

    public TarArchive(File archive) throws IOException {
        this.tarFile = init(archive);
        this.properties = new Properties();
    }

    private File init(File archive) throws IOException { 
        createParent(archive);
        
        if (!archive.exists()) {
            if (archive.createNewFile()) {          
            	LOGGER.debug("Tar Archive created successfully: " + archive.getName());
            } else {
            	LOGGER.warn("Tar Archive failed to create: " + archive.getName());
                return null;
            }
        }

        if (!archive.getName().endsWith(Archive.TAR_EXTENSION)) {
        	LOGGER.info("Tar Archive: " + archive.getName() + " does not have the correct extension (" + Archive.TAR_EXTENSION + ")");
            return null;
        }

        fileNames = new ArrayList<String>();
        archiveSize = 0;
        return archive;
    }

    private void createParent(File archive) throws IOException {
        if (archive != null) {
           String parent = archive.getParent();
           if (parent != null) {
               File parentDir = new File(parent);
               if (parentDir != null && !parentDir.exists()) {
                   parentDir.mkdir();
               }
           }
        }          
    }
    
    public boolean addFilesToArchive(List<GbsFile> files, String consumerName, String syncId) {
        File archiveProps = null;
        
        // KRJ 2016-08-31: Converted out creation to "try with resources"
        // based on an HP Fortify recommendation
                
        try (TarOutputStream out = new TarOutputStream (new GZIPOutputStream(new FileOutputStream(tarFile))))  {
                  
            properties.put(CONSUMER_CONTENT_SET, consumerName);
            properties.put(SYNC_KEY, syncId);
            archiveProps = createArchiveProperties(properties);
            addToTar(out, archiveProps, Archive.ARCHIVE);
            
            for (GbsFile file : files) {
                // Add file to TAR file
                addToTar(out, file.getFile(), file.getPathFromRoot());               
                fileNames.add(file.getFile().getName()); 
                archiveSize += file.getFile().length();
            }
        } catch (IOException e) {
        	LOGGER.error("Failed to create Archive", e);
            return false;
        } finally {
            
            if (archiveProps != null && !archiveProps.delete()) {
            	LOGGER.warn("Archive Properties File did not get deleted: " + archiveProps.getAbsolutePath());
            }
        }
        return true;
    }

    public String getContentSet() {
        loadProperties();
        return properties.getProperty(CONSUMER_CONTENT_SET);
    }
    
    public String getSyncKey() {
        loadProperties();
        return properties.getProperty(SYNC_KEY);
    }
    
    private void loadProperties() {
        if (!properties.isEmpty()) {
            return;
        }
        
        TarInputStream tin = null;
        try {
            try (TarInputStream tarInputStream = new TarInputStream(new GZIPInputStream(new FileInputStream(tarFile)))) {
                tin = tarInputStream;
            }
            //tin = new TarInputStream(new GZIPInputStream(new FileInputStream(tarFile)));
            TarEntry tarEntry = tin.getNextEntry();

            while (tarEntry != null && !tarEntry.isDirectory()) {
                String fileName = tarEntry.getName();
                if (fileName.equals(Archive.ARCHIVE)) {
                    properties.load(tin);
                    break;
                }
                tarEntry = tin.getNextEntry();
            }
        } catch (FileNotFoundException e) {
        	LOGGER.error("File not found trying to retrieve archive properties file", e);
        } catch (IOException e) {
        	LOGGER.error("Output Exception while trying to retrieve archive properties file", e);
        } finally {
            IOUtils.closeQuietly(tin);
        }
    }

    /**
     * Creates Consumer ContentSet File
     * 
     * @param consumerName
     * @return Properties file with ConsumerContentSetName
     */
    private File createArchiveProperties(Properties props) {
        File file = new File(tarFile.getParentFile(), Archive.ARCHIVE);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            props.store(fos, null);
        } catch (FileNotFoundException e) {
        	LOGGER.error("File not found: " + file.getAbsolutePath(), e);
            return null;
        } catch (IOException e) {
        	LOGGER.error("File Output Exception: " + file.getAbsolutePath(), e);
            return null;
        } finally {
            IOUtils.closeQuietly(fos);
        }
        return file;
    }

    public void extractFilesToPath(String filePath, ExtractProgress pathCallback) throws IOException {
        File path = new File(filePath);
        if (!path.exists()) {
        	LOGGER.warn("Attempted to extract to path that does not exist: "+filePath);
            return;
        }
        LOGGER.info("Extracting files for synchronization "+getSyncKey());
        
        //TarInputStream tin = new TarInputStream(new GZIPInputStream(new FileInputStream(tarFile)));
        
        TarInputStream tin;
        try (TarInputStream tarInputStream = new TarInputStream(new GZIPInputStream(new FileInputStream(tarFile)))) {
            tin = tarInputStream;
        }
        TarEntry tarEntry;
        try {
            while ((tarEntry = tin.getNextEntry()) != null) {
                if (tarEntry.getName().equals(Archive.ARCHIVE)) {
                    continue;
                }
                
                fileNames.add(tarEntry.getName());
                archiveSize += tarEntry.getSize();
                
                File destPath = new File(path, tarEntry.getName());
                LOGGER.debug("Processing " + destPath.getAbsoluteFile());
                
                if (tarEntry.isDirectory()) {
                    if(!destPath.exists()) {
                        destPath.mkdir();
                        pathCallback.success(tarEntry.getName(), tarEntry.getSize(), ContentEventAction.ADD);
                    }
                } else {
                    /*
                     * If file does not exist, extract file. If file does exist, check modified time, then extract if it is less than the modified time in
                     * the archive
                     */
                    if (!destPath.exists() || (destPath.exists() && (destPath.lastModified() < tarEntry.getModTime().getTime()))) {
                        boolean updated = destPath.exists();
                        FileOutputStream fout = new FileOutputStream(destPath);
                        try {
                            tin.copyEntryContents(fout);
                        } finally {
                            fout.close();
                        }
                        destPath.setLastModified(tarEntry.getModTime().getTime());
                        pathCallback.success(tarEntry.getName(), tarEntry.getSize(), updated ? ContentEventAction.UPDATE : ContentEventAction.ADD);
                    } else {
                        //log.info(String.format("File: %s Sync %s skipping entry: %s %s, exists %s, modTime %d tarModTime %d", getSyncKey(), tarFile.getName(), destPath.getAbsolutePath(), tarEntry.getName(), String.valueOf(destPath.exists()), destPath.lastModified(), tarEntry.getModTime().getTime()));
                        pathCallback.skipped(tarEntry.getName(), tarEntry.getSize());
                    }
                }
            }
            LOGGER.info("Extraction complete for synchronization "+getSyncKey());
        } finally {
            IOUtils.closeQuietly(tin);
        }
    }

    /**
     * Adds a file to a Tar OutputStream
     * 
     * @param tos -
     *            outputstream to write file
     * @param inFile -
     *            file to read and write to tos
     * @param name -
     *            name of entry
     * @return - true if successful, false otherwise
     */
    private boolean addToTar(TarOutputStream tos, File inFile, String name) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
       
        try {
            if (inFile.exists() && inFile.isFile()) {
                // Create a file input stream and a buffered input stream.
                fis = new FileInputStream(inFile);
                bis = new BufferedInputStream(fis);

                // Create a Tar Entry and put it into the archive (no data yet).
                TarEntry fileEntry = new TarEntry(inFile);
                fileEntry.setModTime(inFile.lastModified());
                fileEntry.setName(name);
                tos.putNextEntry(fileEntry);
                
                // Create a byte array object named data and declare byte count
                // variable.
                byte[] data = new byte[BUFFER_SIZE];
                int byteCount;

                // Create a loop that reads from the buffered input stream and writes
                // to the tar output stream until the bis has been entirely read.
                while ((byteCount = bis.read(data)) > -1) {
                    tos.write(data, 0, byteCount);
                }

                bis.close();
                fis.close();
                tos.closeEntry();
                LOGGER.debug("Successfully added file: " + inFile.getName() + " to Archive");
                return true;
            }

        } catch (IOException e) {
        	LOGGER.warn("File: " + inFile.getName() + " was not added to Archive", e);
        } finally {
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(fis);
        }
        return false;
    }

    public File getArchive() {
        return tarFile;
    }
    
    public List<String> getFileNames() {
        //it will not include the property file
        return fileNames;
    }
    
    public long getSize() {
        return archiveSize;
    }
    
    public String getName() {
        return tarFile.getName();
    }
}
