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

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.solers.delivery.transport.gbs.poll.ExtractProgress;

/**
 * An Archive on files
 * 
 * @author dthemistokleous
 * 
 */
public interface Archive {

    String TAR_EXTENSION = ".tar.gz";

    String ZIP_EXTENSION = ".zip";

    String ARCHIVE = "archive.properties";

    /**
     * Gets the archive
     * 
     * @return - the archive
     */
    File getArchive();

    
    boolean addFilesToArchive(List<GbsFile> files, String consumerName, String syncId);

    /**
     * Gets ContentSet that belongs to this archive
     * 
     * @return
     */
    String getContentSet();
    
    String getSyncKey();
    
    void extractFilesToPath(String path, ExtractProgress pathCallback) throws IOException;

    long getSize();
    List<String> getFileNames();
    String getName();
}
