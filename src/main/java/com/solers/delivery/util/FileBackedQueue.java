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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Configurable;

/**
 * FileBackedQueue is used to restrict the amount of memory used by a single
 * queue data structure. The goal is the fill the queue with a limited number of
 * entries. Once that limit is reached, the queue is paged out to disk, which
 * reduces the amount of memory used.
 * 
 * @author ABajek
 * @author JGimourginas
 */
@Configurable("fileBackedQueue")
public class FileBackedQueue<K> implements Deque<K> {

	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		FileBackedQueue.class);
    
    private static final boolean IS_DEBUG_ENABLED = LOGGER.isDebugEnabled();

    // Set through Spring
    private int maxInMem;
    private int pageSize;
    private File workingDirectory;
    
    private final LinkedList<K> list = new LinkedList<K>();
    private File pageFile;
    private int sizeOnDisk = 0;
    private int sizeInMem = 0;
    private int pageIndex = -1;
    private ObjectInputStream pageInStream;
    private ObjectOutputStream pageOutStream;

    public FileBackedQueue() {
        //use @Configurable to wire
    }
    
    public FileBackedQueue(int maxInMem, int pageSize, String workingDirectory) {
        this(maxInMem, pageSize, new File(workingDirectory));
    }

    public FileBackedQueue(int maxInMem, int pageSize, File dir) {
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Creating FileBackedQueue");
        // mkdirs returns false if directory is already there, so attempt to
        // make dir and THEN check to see if it's
        // there
        dir.mkdirs();
        if (!dir.exists()) {
            throw new IllegalArgumentException("workingDirectory must be able to be created on file system");
        }
        if (pageSize > maxInMem)
            throw new IllegalArgumentException("maxInMemory must be larger than pageSize.");
        if (pageSize <= 0)
            throw new IllegalArgumentException("pageSize must be > 0.");
        if (maxInMem <= 0)
            throw new IllegalArgumentException("maxInMemory must be > 0.");
        this.maxInMem = maxInMem;
        this.pageSize = pageSize;
        this.workingDirectory = dir;
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("FileBackedQueue Created");
    }

    public File getFileBacking() {
        return pageFile;
    }

    /**
     * Puts an item at the end of the queue.
     * 
     * @param o
     * @throws IOException
     */
    private void enqueue(K o) {
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Enqueing " + o);
        pageOut();
        list.add(o);
        sizeInMem++;
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Enqueue complete");
        return;
    }

    /**
     * Removes an item from the front of the queue.
     * 
     * @return
     * @throws IOException
     */
    private K dequeue() {
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Dequeuing");
        if (size() == 0)
            throw new NoSuchElementException("Queue is empty!");
        pageIn();
        K k = list.remove(0);
        sizeInMem--;
        if (sizeOnDisk > 0)
            pageIndex--;
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Dequeued " + k);
        return k;
    }

    /**
     * Puts an object on the front of the queue.
     * 
     * @param o
     * @throws IOException
     */
    private void requeue(K o) {
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Requeuing " + o);
        list.add(0, o);
        sizeInMem++;
        if (sizeOnDisk > 0)
            pageIndex++;
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Requeue complete");

    }

    private void pageOut() {
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Paging out");
        if (list.size() >= maxInMem) {
            if (IS_DEBUG_ENABLED)
            	LOGGER.debug("List size >= maxInMem");
            try {
                openPageFile();
                List<K> l = null;
                if (sizeOnDisk > 0) {
                    l = list.subList(pageIndex, Math.min(list.size(), pageIndex + pageSize + 1));
                } else { // in this case we need to pull from where the page
                    // index is to prevent a screwing up the queue order on
                    // pagein.
                    l = list.subList(list.size() - pageSize, list.size());
                    pageIndex = list.size() - l.size();
                }
                 for (Object i : l) {
                     pageOutStream.writeUnshared(i); // write unshared reduces the
                     // amount of information cached
                     // in the OOS handle table.
                     sizeOnDisk++;
                     sizeInMem--;
                 }
                 pageOutStream.flush(); // make sure they actually make it to disk.
                 pageOutStream.reset(); // we must call reset to clear out the OOS
                 // object handle otherwise we run out of
                 // memory.
                 l.clear();
            } catch (IOException ioe) {
            	LOGGER.warn("Paging to disk failed", ioe);
            }
        }
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Page out complete");
    }

    private void openPageFile() throws IOException {
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Opening Page File");
        if (pageFile == null) {
            pageFile = File.createTempFile("page", ".tmp", workingDirectory);
            pageFile.deleteOnExit();
            if (IS_DEBUG_ENABLED)
            	LOGGER.debug("Created new page file " + pageFile.getPath());
        }
        //if (pageOutStream == null)
        //    pageOutStream = new ObjectOutputStream(new FileOutputStream(pageFile));
        
        if (pageOutStream == null)
        {
            //try (FileOutputStream fileOutputStream = new FileOutputStream(pageFile)) {
            //    pageOutStream = new ObjectOutputStream(fileOutputStream);
            //}
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(pageFile))) {
                pageOutStream = objectOutputStream;
            }
        }            
        
        //if (pageInStream == null)
        //    pageInStream = new ObjectInputStream(new FileInputStream(pageFile));
        
        if (pageInStream == null)
        {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(pageFile))) {
                pageInStream = objectInputStream;
            }
        }
        
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Page File opened");
    }

    @SuppressWarnings("unchecked")
    private void pageIn() {
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Paging in");
        try {
            if (pageIndex == 0) {
                while (sizeOnDisk > 0 && pageIndex < pageSize) {
                    Object o = pageInStream.readObject();
                    sizeOnDisk--;
                    list.add(pageIndex, (K) o);
                    sizeInMem++;
                    pageIndex++;
                }
                if (sizeOnDisk == 0) {
                    closePageFile();
                }
            }
        } catch (ClassNotFoundException e) {
            // This should never happen.
            // We wrote out the class so we should be able to find it.
            throw new RuntimeException("Error loading paged in class", e);
        } catch (IOException ioe) {
        	LOGGER.warn("Failed to page in queue", ioe);
        }
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Page in complete");
    }

    private void closePageFile() {
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Closing page file");
        
        try {
            IOUtils.closeQuietly(pageInStream);
            IOUtils.closeQuietly(pageOutStream);
        } finally {
            pageInStream = null;
            pageOutStream = null;
        }
        
        if (pageFile != null) {
            pageFile.delete();
            pageFile = null;
        }
        
        if (IS_DEBUG_ENABLED)
        	LOGGER.debug("Page file closed");
    }

    @Override
    public boolean isEmpty() {
        return sizeInMem == 0 && sizeOnDisk == 0;
    }

    @Override
    public int size() {
        return sizeInMem + sizeOnDisk;
    }

    protected void finalize() {
        closePageFile();
    }

    @Override
    public boolean add(K e) {
        enqueue(e);
        return true;
    }

    @Override
    public K element() {
        return list.element();
    }

    @Override
    public boolean offer(K e) {
        if (list.size() >= maxInMem) return false;
        add(e);
        return true;
    }

    @Override
    public K peek() {
        return list.peek();
    }

    @Override
    public K poll() {
        try {
            return dequeue();
        } catch (NoSuchElementException nsee) {
            return null;
        }
    }

    @Override
    public K remove() {
        return dequeue();
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        boolean changed = false;
        for (K k : c) {
            changed |= add(k);
        }
        return changed;
    }

    @Override
    public void clear() {
        list.clear();
        sizeInMem = 0;
        sizeOnDisk = 0;
        closePageFile();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addFirst(K e) {
        requeue(e);
    }

    @Override
    public void addLast(K e) {
        add(e);
    }

    @Override
    public Iterator<K> descendingIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public K getFirst() {
        if (isEmpty()) throw new NoSuchElementException();
        return list.getFirst();
    }

    @Override
    public K getLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offerFirst(K e) {
        if (list.size() >= maxInMem) return false;
        requeue(e);
        return true;
    }

    @Override
    public boolean offerLast(K e) {
        return offer(e);
    }

    @Override
    public K peekFirst() {
        return peek();
    }

    @Override
    public K peekLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public K pollFirst() {
        return poll();
    }

    @Override
    public K pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public K pop() {
        return removeFirst();
    }

    @Override
    public void push(K e) {
        addFirst(e);
    }

    @Override
    public K removeFirst() {
        return dequeue();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public K removeLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }
}
