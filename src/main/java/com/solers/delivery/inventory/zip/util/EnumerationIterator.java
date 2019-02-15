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
package com.solers.delivery.inventory.zip.util;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterator<T> implements Iterable<T> {
    protected final Enumeration<T> enumeration;
    public EnumerationIterator(Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new Adapter();
    }

    public static <K> Iterable<K> iterable(Enumeration<K> enumeration) {
        return new EnumerationIterator<K>(enumeration);
    }
    
    public static <K> Iterator<K> iterator(Enumeration<K> enumeration) {
        return new EnumerationIterator<K>(enumeration).iterator();
    }
    
    protected class Adapter implements Iterator<T> {
        @Override
        public boolean hasNext() {
            return enumeration.hasMoreElements();
        }

        @Override
        public T next() {
            return enumeration.nextElement();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
}
