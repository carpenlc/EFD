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
package com.solers.delivery.transport.gbs.poll;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.management.GbsPollerStatus;
import com.solers.delivery.transport.gbs.AbstractGBSService;
import com.solers.delivery.transport.gbs.Archive;
import com.solers.delivery.transport.gbs.GbsTransferStatus;
import com.solers.delivery.transport.gbs.TarArchive;
import com.solers.delivery.transport.gbs.GBSConfigurator;
import com.solers.jmx.registry.AutoRegister;
import com.solers.jmx.registry.AutoRegisterObjectName;
import com.solers.util.NamedThreadFactory;


@AutoRegister(category="GBS Poller Services")
public class GbsPoller extends AbstractGBSService implements GbsPollerStatus {

    /* Logger for Poller */
    private static final Logger log = Logger.getLogger(GbsPoller.class);

    /* Interval to poll a directory */
    private final long pollInterval;

    /* Directory to poll */
    private final File dir;

    /* List of PollManagers */
    private List<PollListener> listeners;

    /* Scheduled Executor Service for Polling Service */
    private ScheduledExecutorService scheduler;

    private static final int MIN_POLL_LIMIT = 999;

    // Active GBS content sets
    private final Map<String, Long> activeMap;    
    private final Map<String, Long> sizeMap;
    
    //GbsPollerStatus related fields
    private boolean isRunning = false;
    private final CopyOnWriteArrayList<GbsTransferStatus> transferStats;
    private int numberOfFiles;
    private int numberOfFilesCompleted;
    
    public GbsPoller(String dir, List<PollListener> listeners, long pollInterval) {
        this.dir = new File(dir);
        this.listeners = listeners;
        this.pollInterval = pollInterval;
        
        activeMap = new HashMap<String, Long>();
        sizeMap = new HashMap<String, Long>();
        transferStats = new CopyOnWriteArrayList<GbsTransferStatus>();
    }

    public void start() {
        if (GBSConfigurator.isGBSEnabled()) {
            if (pollInterval <= MIN_POLL_LIMIT) {
                throw new IllegalStateException("Polling Interval needs to be Greater than 999 ms");
            }

            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Directory: " + dir.getAbsolutePath() + " for polling cannot be created");
            } else if (!dir.isDirectory()) {
                throw new IllegalStateException("Directory: " + dir.getAbsolutePath() + " for polling is not a directory");
            } else if (!dir.canRead()) {
                throw new IllegalStateException("Cannot read from polling directory: " + dir.getAbsolutePath());
            }

            if (listeners.size() <= 0) {
                log.debug("No PollListeners configured");
                return;
            }
            scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(this.getClass().getSimpleName()));
            scheduler.scheduleWithFixedDelay(this, 0, pollInterval, TimeUnit.MILLISECONDS);
            log.debug("Polling is enabled on directory: " + dir.getAbsolutePath());
        } else {
            log.debug("Polling is disabled");
        }
    }

    /**
     * This method runs after every scheduled interval. This method will retrieve the files in the polling directory and pass the file along to all
     * poll managers.
     */
    public void run() {
        isRunning = true;

        try {
            File[] allFiles = dir.listFiles();      
            List<File> files = updateSizeMap(allFiles);
            
            numberOfFiles = allFiles.length;
            numberOfFilesCompleted = files.size();
            
            log.debug("Start Polling directory: " + dir.getAbsolutePath() + " found " + numberOfFilesCompleted + " entrie(s)");
            
            process(files);
        } catch (Exception ex) {
            log.error("Unexpected error caught processing directories", ex);
        }
        
        log.debug("Finished Polling directory: " + dir.getAbsolutePath());
        isRunning = false;
    }
    
    private void process(List<File> files) {
        for (File file : files) {
            Archive archive = null;
            try {
                archive = new TarArchive(file);
            } catch (IOException e) {
                log.error("Failed to retrieve Archive from file: " + file.getAbsolutePath(), e);
                archive = null;
            }

            if (archive != null) {                              
                String consumerName = archive.getContentSet();
                String syncKey = archive.getSyncKey();
                ContentSet cs = getContentSet(activeMap.get(consumerName));
                if (cs != null) {
                    createTransferStatus(archive, cs.getId());
                    for (PollListener listener : listeners) {
                        listener.onAdd(archive, cs.getPath(), syncKey, cs);
                    }
                } else {
                    log.info("ConsumerContentSet " + consumerName + " does not exist, archive will be removed");
                }                               
            }
            removeFile(file);
        }

    }

    private void createTransferStatus(Archive archive, Long id) {
       GbsTransferStatus status = new GbsTransferStatusImpl(
                                       archive.getName(),
                                       archive.getSize(),
                                       archive.getFileNames(),
                                       archive.getContentSet(),
                                       archive.getSyncKey());   
       transferStats.add(status);
    }
    
    /**
     * Called by Spring when the bean is being destroyed
     * 
     */
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.deleteOnExit();
            }
        }
        transferStats.clear();
    }

    /**
     * Keeps track of file sizes, when the file size does not change, we ASSUME the file is fully uploaded
     * 
     * @param files
     * @return
     */
    private List<File> updateSizeMap(File[] files) {
        List<File> completeFiles = new ArrayList<File>();
        Long currSize = null;

        for (File file : files) {
            currSize = sizeMap.get(file.getName());
            if (currSize == null || currSize != file.length()) {
                sizeMap.put(file.getName(), file.length());
            } else {
                log.debug("File: " + file.getAbsolutePath() + " has completed downloading");
                sizeMap.remove(file.getName());
                completeFiles.add(file);
            }
        }

        return completeFiles;
    }

    /**
     * Removes the file once it has been processed by all listeners
     * 
     * @param file -
     *            file to remove
     */
    protected static void removeFile(File file) {
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to remove File: " + file.getAbsolutePath(), e);
            }
        }
    }

    //JMX Methods
    @AutoRegisterObjectName
    public String getId() {
        try {
           return "GbsPoller_" + java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException uhe) {
           return "GbsPoller";  
        }
    }
    
    //GbsPollerStatus methods
    public List<GbsTransferStatus> getStatus() {
        return transferStats;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public int getNumberOfFiles() {
        return numberOfFiles;
    }
    
    public int getNumberOfFilesCompleted() {
        return numberOfFilesCompleted;
    }   
    //End of JMX methods
    
    protected void doStart(Long id) {
        activeMap.put(getContentSetName(id), id);
    }

    protected void doStop(Long id) {
        activeMap.remove(getContentSetName(id));
    }

    private static class GbsTransferStatusImpl implements GbsTransferStatus {
       private String archiveName;
       private long archiveSize;
       private List<String> fileNames;
       private String contentSetName;
       private String syncKey;
   
       public GbsTransferStatusImpl(String archiveName,
                                long archiveSize,
                                List<String> fileNames,
                                String contentSetName,
                                String syncKey) {
          this.archiveName = archiveName;
          this.fileNames = fileNames;
          this.contentSetName = contentSetName;
          this.syncKey = syncKey;
       }

       public String getArchiveName() {
           return archiveName;
       }

       public long getArchiveSize() {
           return archiveSize;
       }

       public List<String> getFileNames() {
           return fileNames;
       }

       public String getContentSetName() {
           return contentSetName;
       }
       
       public String getSyncKey() {
           return syncKey;
       }
    }
}
