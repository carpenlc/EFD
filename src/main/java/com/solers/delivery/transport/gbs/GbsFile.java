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

/**
 * Wrapper of the file requested over GBS with its pathFromRoot. Used to know 
 * what the name of the file should look like in the transported Archive.
 * 
 * @author dthemistokleous
 */
public class GbsFile {

    /* File requested for transfer */
    private final File file;

    /* Path from Root of file requested for transfer */
    private final String pathFromRoot;

    public GbsFile(File file, String pathFromRoot) {
        this.file = file;
        this.pathFromRoot = pathFromRoot;
    }

    public File getFile() {
        return file;
    }

    public String getPathFromRoot() {
        return pathFromRoot;
    }
}
