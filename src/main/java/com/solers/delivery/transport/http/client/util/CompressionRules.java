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
package com.solers.delivery.transport.http.client.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

public class CompressionRules {

    private List<String> compressibleExtensions;
    private long minimumSizeBytes;

    public CompressionRules() {
        compressibleExtensions = new ArrayList<String>();
    }

    @Required
    public void setCompressibleExtensions(String compressibleExtensions) {
        String[] extensions = compressibleExtensions.split(",");
        for (String extension : extensions) {
            this.compressibleExtensions.add(extension.trim().toUpperCase());
        }
     }
    
    @Required
    public void setMinimumFileSizeBytes(long minimumSizeBytes) {
        this.minimumSizeBytes = minimumSizeBytes;
    }
    
    private String getExtension(String path) {
        if (!path.matches(".*\\.+.+[^\\.]$")) {
            return null;
        } else {
            try {
                return path.substring(path.lastIndexOf('.') + 1, path.length());
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        } 
    }

    public boolean shouldBeCompressed(String path, long bytes) {
        String ext = getExtension(path);
        return (ext == null) ? false :  compressibleExtensions.contains(ext.toUpperCase()) && bytes >= this.minimumSizeBytes ;
    }
}
