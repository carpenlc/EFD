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
package com.solers.delivery.transport.gbs.push;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.domain.FtpConnection;
import com.solers.delivery.transport.gbs.AbstractGBSService;
import com.solers.delivery.transport.gbs.GbsFile;
import com.solers.delivery.transport.gbs.GBSConfigurator;
import com.solers.delivery.transport.gbs.TransportType;
import com.solers.security.EncryptionUtils;
import com.solers.util.NamedThreadFactory;

/**
 * Transports all files through GBS onto the SBM via FTPS
 * 
 * @author dthemistokleous
 */
public class GbsPusher extends AbstractGBSService {
    /* Logger */
    private static final Logger log = Logger.getLogger(GbsPusher.class.getName());
    private static final int SIZE = 1024 * 1024;
    private static final int TIME = 1000 * 60;

    /* Maximum size of a queue before it gets transported */
    private final Long maximumAllowableQueueSize;

    /* Maximum time to wait before a queue gets transported */
    private final Long maximumAllowableQueueTime;

    private final File archiveDirectory;
    //private Map<Long, ContentSet> contentSetMap;
    private final Map<String, ConcurrentHashMap<String, List<GbsFile>>> contentSetFileMap;

    /* Executor that transfers files to GBS */
    private ExecutorService executorService = null;

    /* After a fix delay it calls the run method to submit tasks for transfer */
    private final ScheduledExecutorService scheduler;
    
    private final boolean secure;
    
    /**
     * GbsSupplier constructor
     * 
     * @param config -
     *            GbsConfiguration
     * @param maxQueueSize -
     *            maximum size of queue (MB)
     * @param maxQueueTime -
     *            maximum time of queue (MIN)
     */
    public GbsPusher(Long maxQueueSize, Long maxQueueTime, File archiveDirectory, boolean secure) {
        this.maximumAllowableQueueSize = maxQueueSize * SIZE;
        this.maximumAllowableQueueTime = maxQueueTime * TIME;
        this.archiveDirectory = archiveDirectory;
        this.secure = secure;
        this.contentSetFileMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, List<GbsFile>>>();
        scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(this.getClass().getSimpleName()));
    }

    /**
     * Initializes this object, called after constructor by Spring only if GBS is enabled
     */
    public void init() {
        if (GBSConfigurator.isGBSEnabled()) {
            executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("GbsTransferExecutor"));

            if (maximumAllowableQueueSize < 0) {
                throw new IllegalStateException("Need to supply a Positive or Zero MaximumAllowableQueueSize");
            }

            // calculates the size from bytes into MB
            if (maximumAllowableQueueTime < 0) {
                throw new IllegalStateException("Need to supply a Positive or Zero MaximumAllowableQueueTime");
            }
            // calculates minutes
            if (maximumAllowableQueueTime > 0) {
                // schedule a time to call run() only if maximum queue time is > 0
                scheduler.scheduleWithFixedDelay(this, 0, maximumAllowableQueueTime, TimeUnit.MILLISECONDS);
                log.debug("Successfully Scheduled the GbsSupplierTask");
            }
        }
    }

    /**
     * Timer task to remove all files from the queue and send them to the SBM
     */
    public synchronized void run() {
        log.debug("Running Timer GbsSupplier Task - removing all files from queue and transporting");

        for (String supplierName : contentSetFileMap.keySet()) {
            Map<String, List<GbsFile>> queues = contentSetFileMap.get(supplierName);
            for (String key : queues.keySet()) {
                log.debug("Adding archive with supName" + supplierName + " to Transport: " + key);
                transport(supplierName, key);
            }
        }

        log.debug("Successfully Removed all files from Queue");
    }

    /**
     * Adds file to the queue for delivery which is referenced by GbsKey This needs to be synchronized because a scheduled thread at a given interval will
     * remove all files from the queues and upload them to the SBM
     * 
     * @param key -
     *            key of queue to add
     * @param file -
     *            file to transport
     */
    public synchronized void addFile(String consumerContentSet, String syncKey, String supplierContentSet, String host, File file, String path) {
        String key = generateKey(consumerContentSet, host, syncKey);
        ConcurrentHashMap<String, List<GbsFile>> queues = contentSetFileMap.get(supplierContentSet);

        queues.putIfAbsent(key, new ArrayList<GbsFile>());

        List<GbsFile> files = queues.get(key);

        if (files != null) {
            addFileToList(files, new GbsFile(file, path));

            if (getListFileSize(files) >= maximumAllowableQueueSize) {
                transport(supplierContentSet, key);
            }
        } else {
            log.warn("File Queue List for Key: " + key + " is NULL");
        }
    }

    /**
     * Retrieve the total file size of the list of files
     * 
     * @param files -
     *            files to get size of
     * @return - total size of all files
     */
    private Long getListFileSize(List<GbsFile> files) {
        Long total = Long.valueOf(0);
        for (GbsFile file : files) {
            total += file.getFile().length();
        }
        return total;
    }

    /**
     * Adds the file to the list of files
     * 
     * @param files -
     *            list to add file to
     * @param file -
     *            file to add
     */
    private void addFileToList(List<GbsFile> files, GbsFile file) {
        // if file does not already exist in the list, add it
        if (!files.contains(file)) {
            files.add(file);
        } else {
            log.info("File " + file.getFile().getName() + " already exists in the file list, will not be added again");
        }
    }

    /**
     * Removes the Queue from the fileMap and places it on the transportQueue
     * 
     * @param csName -
     *            supplierContentSetName
     * @param key -
     *            key of supplier (consumer:host:syncId)
     */
    private void transport(String supplierName, String key) {
        // remove both(queueSizeMap and fileMap) queues from the appropriate maps
        Map<String, List<GbsFile>> queues = contentSetFileMap.get(supplierName);
        List<GbsFile> files = queues.remove(key);
        FtpConnection connection = null;

        if (files.size() > 0) {
            // add queue to transportQueue
            GbsTransferTask task = null;
            connection = getConnection(supplierName);
            TransportType type = secure ? TransportType.FTPS :TransportType.FTP;
          
            task = new GbsTransferTask(type, getConsumerName(key), getSyncKey(key),
                                       files, connection, archiveDirectory);
            executorService.submit(task);
            log.debug("Successfully added files to ZipQueue for ContentSetId = " + key);
        }
        log.debug("No files in Queue for Supplier: " + supplierName);
    }

    /**
     * Gets the FtpConnection from the Map
     * 
     * @param csName
     * @return
     */
    private FtpConnection getConnection(String csName) {
        FtpConnection connection = null;
        Map<Long, ContentSet> contentSetMap = getContentSets();
        for (ContentSet cs : contentSetMap.values()) {
            if (cs.getName().equals(csName)) {
                connection = new FtpConnection(cs.getFtpConnection());
                // A way to put the PLAIN TEXT password in the FtpConnecion object
                
                // TODO This needs to change!  The secret shouldn't be embedded here
                connection.setPassword(EncryptionUtils.decryptFromHex("gbs_key".toCharArray(), connection.getPassword()));
                return connection;
            }
        }
        return null;
    } 
    
    /**
     * Generates the Key for the file buckets
     * 
     * @param consumerContentSet
     * @param host
     * @param syncKey
     * @return
     */
    private String generateKey(String consumerContentSet, String host, String syncKey) {
        return consumerContentSet + ":" + host + ":" + syncKey;
    }

    /**
     * Retrieves Consumer Name from the key
     * 
     * @param key -
     *            key
     * @return - consumer name
     */
    private String getConsumerName(String key) {
        return key.split(":")[0];
    }
    
    private String getSyncKey(String key) {
        return key.split(":")[2];
    }

    protected void doStart(Long id) {
        contentSetFileMap.put(getContentSetName(id), 
                    new ConcurrentHashMap<String, List<GbsFile>>());
    }

    protected void doStop(Long id) {
        contentSetFileMap.remove(getContentSetName(id));
    }

    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }

        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
