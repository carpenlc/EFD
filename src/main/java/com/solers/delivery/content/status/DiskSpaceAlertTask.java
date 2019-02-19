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
package com.solers.delivery.content.status;

import java.io.File;
import java.math.BigDecimal;

import org.apache.log4j.Logger;

import com.solers.delivery.alerts.AlertService;
import com.solers.delivery.content.ContentService;
import com.solers.delivery.domain.Alert;
import com.solers.delivery.domain.ContentSet;

/**
 * @author <a href="mailto:michael.yingling@solers.com">Michael Yingling</a>
 */
public class DiskSpaceAlertTask implements Runnable {

    private static final Logger log = Logger.getLogger(DiskSpaceAlertTask.class);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100L);
    
    private final BigDecimal threshold;
    private final AlertService alerts;
    private final ContentService application;
    private final String root;

    public DiskSpaceAlertTask(ContentService cs, AlertService as, double warning_threshold, String efd_path) {
        application = cs;
        alerts = as;
        threshold = BigDecimal.valueOf(warning_threshold);
        root = efd_path;
    }

    @Override
    public void run() {
        log.info("Running");

        // For measuring the disk space of the EFD installation directory
        measure(root);

        for (ContentSet supplier : application.getContentSets()) {
            measure(supplier.getPath());
        }
    }
    
    private void measure(String cs_path) {
        File path = new File(cs_path);
        
        if (!path.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Path: "+path+" does not exist.  Will not measure.");
            }
            return;
        }
        
        BigDecimal free = BigDecimal.valueOf(getFreeSpace(path));
        BigDecimal total = BigDecimal.valueOf(getTotalSpace(path));
        
        if (total.equals(BigDecimal.ZERO)) {
            log.warn("Zero total space for "+cs_path+".  Cannot measure disk space");
            return;
        }
        
        BigDecimal used = total.subtract(free);
        BigDecimal inuse = used.divide(total, 2, BigDecimal.ROUND_HALF_DOWN).multiply(ONE_HUNDRED);
        
        if (log.isDebugEnabled()) {
            log.debug("Path: "+path);
            log.debug("Threshold: "+threshold);
            log.debug("Free: "+free);
            log.debug("Total: "+total);
            log.debug("Used: "+used);
            log.debug("In use: "+inuse);
            log.debug("Comparison: "+inuse.compareTo(threshold));
        }
        
        int comparison = inuse.compareTo(threshold);
        // Saves an Alert for the log when the file's size warning threshold is met or exceeded
        if (comparison >= 0) {
            String message = "WARNING: Output path (" + cs_path + ") is " + inuse + "% full.";
            log.warn(message);
            alerts.save(new Alert(message, Alert.AlertType.USER));
        }
    }
    
    protected long getFreeSpace(File path) {
        return path.getFreeSpace();
    }
    
    protected long getTotalSpace(File path) {
        return path.getTotalSpace();
    }
}
