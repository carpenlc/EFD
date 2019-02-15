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
package com.solers.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The SoftCache emulates several of the Map API methods, while providing
 * memory-sensitive caching metrics.  There are two parts to the SoftCache:
 * the HEAP, and the CACHE.  The size of the heap may be controlled via
 * a constructor property.  The heap is a region dedicated to hard references.
 * Once the size of the heap is exceeded, objects are placed on the CACHE
 * using soft references.  You must supply a Reconstructor to the SoftCache
 * when you create one.  This reconstructor is passed the key of a garbage-collected
 * value and is expected to rebuild the object.
 *  
 * @author gvanore
 */
public class SoftCache<K, V> implements Iterable<Map.Entry<K, V>> {
    public static final int DEFAULT_HEAP_SIZE = 10000;
    
    protected final Map<K, V> heap = new HashMap<K, V>();
    protected final Map<K, Reference<V>> backing = new HashMap<K, Reference<V>>();
    private final Reconstructor<K, V> reconstructor;
    private final int heapSize;
    
    public SoftCache(int heapSize, Reconstructor<K, V> reconstructor) {
        this.heapSize = heapSize;
        this.reconstructor = reconstructor;
    }
    
    public SoftCache(Reconstructor<K, V> reconstructor) {
        this(DEFAULT_HEAP_SIZE, reconstructor);
    }
    
    /**
     * Puts the value in the cache under the specified key.  If 
     * the size of the map is less than the size of the heap, a 
     * hard reference is used.  Otherwise, a soft reference is used.
     * @param key
     * @param value - cannot be null
     * @return the original value underneath this key (may be null)
     */
    public V put(K key, V value) {
        if (value == null) throw new NullPointerException();
        
        if (heap.size() >= heapSize) {            
            V current = remove(key);
            bury(key, value);
            return current;
        }
        return heap.put(key, value);
    }
    
    /**
     * Return the value stored under the key.
     * @param key
     * @return
     */
    public V get(K key) {
        if (heap.containsKey(key)) return heap.get(key);    
        Reference<V> r = backing.get(key);
        return disinter(key, r);
    }
    
    /**
     * Retrieve the value from the reference
     * @param key
     * @param r
     * @return
     */
    private V disinter(K key, Reference<V> r) {
        if (r == null) return null;
        V value = r.get();
        if (value == null) {
            value = reconstructor.rebuild(key);
            bury(key, value);
        }
        return value;
    }
    
    private void bury(K key, V value) {
        backing.put(key, new SoftReference<V>(value));
    }
    
    public V remove(K key) {
        if (heap.containsKey(key)) {
            return heap.remove(key);
        }
        
        V value = get(key);
        backing.remove(key);
        return value;
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public int size() {
        return heap.size() + backing.size();
    }
    
    public void clear() {
        heap.clear();
        backing.clear();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<java.util.Map.Entry<K, V>> iterator() {
        return new Iterator<java.util.Map.Entry<K, V>>() {
            private final Iterator<Map.Entry<K, V>> heapiter = heap.entrySet().iterator();
            private final Iterator<Map.Entry<K, Reference<V>>> backiter = backing.entrySet().iterator();
            
            private Iterator iter = heapiter;
            
            @Override
            public boolean hasNext() {
                if (heapiter.hasNext()) {
                    return true;
                } else {
                    iter = backiter;
                    return backiter.hasNext();
                }
                    
            }

            @Override
            public java.util.Map.Entry<K, V> next() {
                if (heapiter.hasNext()) {
                    return heapiter.next();
                } else {
                    Map.Entry<K, Reference<V>> entry = backiter.next();
                    K key = entry.getKey();
                    return new Entry(key, get(key));
                }
            }

            @Override
            public void remove() {
                iter.remove();
                
            }
        };
    }
    
    public interface Reconstructor<K, V> {
        V rebuild(K key);
    }
    
    public class Entry implements Map.Entry<K, V> {
        protected final K key;
        protected final V value;
        
        protected Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        
        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            return put(key, value);
        }
    }    
}
