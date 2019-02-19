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
package com.solers.delivery.content.consumer.difference;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Deque;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import com.solers.delivery.content.status.ConsumerProgress;
import com.solers.delivery.daos.DAOFactory;
import mil.nga.efd.controllers.PendingDeleteDAO;
import mil.nga.efd.domain.ConsumerContentSet;
import mil.nga.efd.domain.PendingDelete;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.EventManager;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.InventoryFactory;
import com.solers.delivery.util.FileBackedQueue;

@Configurable
public class Remover implements ContentDifferenceHandler {
    
    private static final Logger log = Logger.getLogger(Remover.class);

    private final ConsumerContentSet contentSet;
    private final int pathSkip;
    
    private EventManager eventManager;
    private DAOFactory daoFactory;
    
    public Remover(ConsumerContentSet contentSet) {
        this.contentSet = contentSet;
        String normalizedPath = FilenameUtils.normalize(contentSet.getPath());
        File file = new File(normalizedPath);
        if (file.getParent() == null) { // Filesystem root
            this.pathSkip = normalizedPath.length();
        } else {
            this.pathSkip = normalizedPath.length() + 1;
        }
    }
    
    @Autowired
    public void setDaoFactory(@Qualifier("hibernateDaoFactory") DAOFactory factory) {
        this.daoFactory = factory;
    }
    
    @Autowired
    public void setEventManager(@Qualifier("eventManager") EventManager manager) {
        this.eventManager = manager;
    }
    
    @Transactional
    public void handle(ContentDifference difference, ConsumerProgress progress) {
        if (log.isDebugEnabled()) {
            log.debug("Next difference to resolve: " + difference.getPath());
        }
        
        if (usingDelay()) {
            markForDeletion(difference, progress);
        } else {
            remove(difference, progress);
        }
    }
    
    @Transactional
    public void cleanup(ConsumerProgress progress) {
        boolean done = false;
        while (done == false) {
            done = processNextPendingDelete(progress);
        }
    }
    
    private boolean usingDelay() {
        return contentSet.getFileDeleteDelay() > 0;
    }
    
    private boolean shouldRemove(File file, PendingDelete pd) {
        return file.exists() && notInInventory(pd);
    }
    
    private void markForDeletion(ContentDifference difference, ConsumerProgress progress) {
        PendingDeleteDAO dao = daoFactory.getPendingDeleteDAO();
        PendingDelete existing = dao.getByPath(difference.getConsumerContentSetId(), difference.getPath());

        if (existing == null) {
            if (log.isDebugEnabled()) {
                log.debug("File " + difference.getPath() + " marked for deletion at " + difference.getTimeAdded().getTime());
            }
            dao.makePersistent(new PendingDelete(difference));
        } else {
            if (log.isDebugEnabled()) {
                log.debug("File " + difference.getPath() + " already marked for deletion");
            }
        }
        
        // If the existing pending delete will be deleted at the end of the synchronization
        // when cleanup() is called, don't log an event or update the progress because
        // cleanup() will do that for us
        if (existing == null || !existing.getTimeMarkedForDeletion().before(delayDate())) {
            ContentEvent contentEvent = createEvent(difference.getPath(), progress);
            contentEvent.setBytesRequested(difference.getSize());
            contentEvent.setBytesManipulated(0);
            contentEvent.setResult(ContentEventResult.PENDING_DELETE);
            eventManager.received(contentEvent);
            
            progress.removed(difference.getSize(), true);
        }
    }
    
    private Calendar delayDate() {
        long delay = contentSet.getFileDeleteDelayUnit().toMillis(contentSet.getFileDeleteDelay());
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(result.getTimeInMillis() - delay);
        return result;
    }

    private boolean processNextPendingDelete(ConsumerProgress progress) {
        PendingDeleteDAO dao = daoFactory.getPendingDeleteDAO();
        
        PendingDelete pd = dao.getNextToDelete(contentSet.getId(), delayDate());
        
        if (pd != null) {
            if (log.isDebugEnabled()) {
                log.debug("Pending Delete to handle next: " + pd.getPath());
            }
            
            File file = new File(contentSet.getPath(), pd.getPath());
            if (shouldRemove(file, pd)) {
                remove(file, progress);
            }
            dao.makeTransient(pd);
        }
        return pd == null;
    }
    
    /**
     * @param pd
     * @return true if {@code pd} is not in the inventory
     */
    private boolean notInInventory(PendingDelete pd) {
        boolean notInInventory = true;
        Inventory inventory = InventoryFactory.getInventory(contentSet);
        try {
            if (inventory != null) {
                notInInventory = inventory.read(pd.getPath()) == null;
            }
        } finally {
            if (inventory != null) {
                inventory.close();
            }
        }
        return notInInventory;
    }

    private long getBytesToRemove(File file) {
        if (file.isDirectory()) {
            return FileUtils.sizeOfDirectory(file);
        } else {
            return file.length();
        }
    }
    
    private void remove(ContentDifference difference, ConsumerProgress progress) {
        File toRemove = new File(contentSet.getPath(), difference.getPath());
        
        if (toRemove.exists()) {
            remove(toRemove, progress);
        }
    }

    /**
     * Removes a file or a directory and all of its contents. Uses a FileBackedQueue to
     * ensure the number of entries in memory is bounded.
     * 
     * @param entry or file to remove
     * @return true if directory and its contents are removed, false otherwise
     * @throws IOException
     *                 if FileBackedQueue has an issue
     */
    public void remove(File entry, ConsumerProgress progress) {
        Deque<File> queue = new FileBackedQueue<File>();
        // put the directory at the front of the queue
        if (log.isDebugEnabled()) {
            log.debug("Beginning to remove entry: " + entry.getPath());
        }
        queue.add(entry);
        while (!queue.isEmpty()) {
            long parentTimestamp = 0;
            File current = queue.remove();
            File parent = current.getParentFile();
            
            // capture parent's timestamp so it can be reset after removal -
            // only needed for Windows OS compliance
            if (parent != null) {
                parentTimestamp = parent.lastModified();
            }
            if (current.isFile() || isEmpty(current)) {
                delete(current, progress);
                // needed for Windows OS, where removal changes parent's TS
                if (parentTimestamp > 0) {
                    parent.setLastModified(parentTimestamp);
                }
            } else {
                // must place parent AFTER all children in the stack - that
                // way, when parent is called the next time it
                // is assured of having no children nodes (and therefore can
                // be removed)
                queue.addFirst(current);
                for (File child : current.listFiles()) {
                    // requeue makes the queue act lact a stack, which is
                    // desired in this case - LIFO
                    queue.addFirst(child);
                }
            }
        }
    }
    
    private boolean isEmpty(File directory) {
        File [] children = directory.listFiles();
        return children == null || children.length == 0;
    }
    
    private void delete(File file, ConsumerProgress progress) {
        boolean isDirectory = file.isDirectory();
        long mtime = file.lastModified();
        String path = FilenameUtils.normalize(file.getPath()).substring(pathSkip);
        long bytesToRemove = getBytesToRemove(file);
        boolean deleted = file.delete();
        
        if (!deleted) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Removal of %s failed, trying again", file.getPath()));
            }
            deleted = file.delete();
        }
        
        if (log.isDebugEnabled()) {
            log.debug(String.format("Removal of %s completed: %s", file.getPath(), deleted));
        }
        
        ContentEvent contentEvent = createEvent(path, progress);
        contentEvent.setBytesRequested(bytesToRemove);
        contentEvent.setBytesManipulated(deleted ? contentEvent.getBytesRequested() : 0);
        contentEvent.setResult(deleted ? ContentEventResult.SUCCESS : ContentEventResult.DELETE_FAILED);
        contentEvent.setDirectory(isDirectory);
        contentEvent.setPathMtime(mtime);
        eventManager.received(contentEvent);
        
        progress.removed(contentEvent.getBytesManipulated(), deleted);
    }
    
    private ContentEvent createEvent(String path, ConsumerProgress progress) {
        ContentEvent contentEvent = new ContentEvent(contentSet.getId());
        
        contentEvent.setHostname(contentSet.getSupplierAddress());
        contentEvent.setContentSetName(contentSet.getName());
        contentEvent.setAction(ContentEventAction.DELETE);
        contentEvent.setMessage("Delete ");
        contentEvent.setSynchronizationId(SynchronizationEvent.getId(progress.getKey(), contentSet.getId()));
        contentEvent.setPath(FilenameUtils.separatorsToUnix(path));
        
        
        return contentEvent;
    }
}
