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
package com.solers.util.dao;

import java.io.Serializable;
import java.util.List;

/**
 * Interface is used as described by Hibernate authors in (http://www.hibernate.org/328.html).  Designed to be the
 * base interface extended by all other persistant entity interfaces.  Goal is to capture basic Create, Read, Update,
 * and Delete (CRUD) operations in this base interface for any persistant entity through use of Generics.
 *
 * @author JGimourginas
 */
public interface GenericDAO<T, ID extends Serializable> {
    T findById(ID id, boolean lock);
    
    T getById(ID id);

    List<T> findAll();

    List<T> findByExample(T exampleInstance, String[] excludeProperty);

    T makePersistent(T entity);

    void makeTransient(T entity);

    void flush();
}
