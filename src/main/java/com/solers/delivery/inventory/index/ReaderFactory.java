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
package com.solers.delivery.inventory.index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.solers.delivery.inventory.Inventory;

public final class ReaderFactory {
    private ReaderFactory() {
        
    }
    
    public static Inventory newInstance(File inventoryFile) throws IOException {
        HeaderStruct header;
        RandomAccessFile in = new RandomAccessFile(inventoryFile, "r");
        try {
            header = HeaderStruct.create(in);
            if (header == null) {
                throw new IOException("Could not read inventory file " + inventoryFile.getPath());
            }
        } finally {
            if (in != null) in.close();
        }
        
        switch (header.version) {
            case 2:
                return new com.solers.delivery.inventory.index.v2.InventoryReader(inventoryFile);
            case 1:
                return new com.solers.delivery.inventory.index.v1.InventoryReader(inventoryFile);
            default:
                throw new IOException("Could not find inventory implementation for version " + header.version);
        }
    }
}
