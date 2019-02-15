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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import com.solers.util.Filter;


public class FilteredEnumerator<E> implements Enumeration<E> {
    //TODO: perhaps fill this in a lazy fashion as a later improvement
    private final Deque<E> fifoStack = new ArrayDeque<E>();
    
    public FilteredEnumerator(Enumeration<? extends E> enumeration, Filter<E> filter) {
        while (enumeration.hasMoreElements()) {
            E next = enumeration.nextElement();
            if (filter.accept(next)) {
                fifoStack.offerLast(next);
            }
        }
    }
    
    @Override
    public boolean hasMoreElements() {
        return !fifoStack.isEmpty();
    }

    @Override
    public E nextElement() {
        if (fifoStack.isEmpty()) {
            throw new NoSuchElementException();
        }
        return fifoStack.pollFirst();
    }

}
