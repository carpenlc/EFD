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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * <p>An immutable list to support pagination. Callers are expected to
 * access the list through {@code subList(int,int)}.  If callers wish,
 * they may iterate the whole collection with {@code iterator} which will
 * fetch pages as need.  Any other operations 
 * throw {@code IllegalStateException}</p>
 * 
 * <p>Subclasses override {@code initialize} and {@code getSubList(int,int)}</p>
 * 
 * <p>{@code initialize} implementors are expected to compute the total size
 * of the list and call {@code initialized(int size)} with the result.
 * All operations throw {@code IllegalStateException} before 
 * {@code initialize} is called</p>
 * 
 * <p>{@code getSubList(int,int)} implementors are expected to return
 * the page of elements that are included in the given range</p>
 *  
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public abstract class PaginatedList<E> implements List<E> {

    public static final EmptyList EMPTY_LIST = new EmptyList();
    
    @SuppressWarnings("unchecked")
    public static <T> PaginatedList<T> emptyList() {
        return (PaginatedList<T>) EMPTY_LIST;
    }
    
    private final int pageSize;
    
    private boolean initialized;
    private int size;

    protected PaginatedList(int pageSize) {
        this.pageSize = pageSize;
        this.initialized = false;
        this.size = 0;
    }
    
    public final List<E> subList(int fromIndex) {
        return subList(fromIndex, this.pageSize+fromIndex);
    }
    
    @Override
    public final boolean isEmpty() {
        return getSize() == 0;
    } 
    
    @Override
    public final int size() {
        return getSize();
    }
    
    @Override
    public final Iterator<E> iterator() {
        checkInitialized();    
        return new PaginatedListIterator();
    }
    
    @Override
    public final List<E> subList(int fromIndex, int toIndex) {
        return Collections.unmodifiableList(getSubList(fromIndex, toIndex));
    }
    
    public final int getSize() {
        checkInitialized();
        return size;
    }

    protected final void initialized(int size) {
        initialized = true;
        this.size = size;
    }

    protected final void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("You must call initialize() first");
        }
    }
    
    protected final int getPageSize() {
        return pageSize;
    }
    
    public abstract void initialize();
    
    protected abstract List<E> getSubList(int fromIndex, int toIndex);

    @Override
    public final void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final E get(int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final ListIterator<E> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final E set(int index, E element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final boolean add(E o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear() {
       throw new UnsupportedOperationException();
    }
    
    @Override
    public final boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }
    
    protected class PaginatedListIterator implements Iterator<E> {
        
        int count = 0;
        Iterator<E> current;
        
        public boolean hasNext() {
            return count < PaginatedList.this.getSize();
        }
        
        public E next() {
            if (current == null || current.hasNext() == false) {
                current = PaginatedList.this.subList(count, PaginatedList.this.getPageSize()+count).iterator();
            }
            count++;
            return current.next();
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    private static class EmptyList extends PaginatedList<Object> {

        public EmptyList() {
            super(0);
            initialize();
        }

        @Override
        protected List<Object> getSubList(int fromIndex, int toIndex) {
            return Collections.emptyList();
        }

        @Override
        public void initialize() {
            initialized(0);
        }
        
    }
}
