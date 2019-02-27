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
package mil.nga.efd.controllers;

import mil.nga.efd.domain.ContentSet;

import java.util.List;

/**
 * @author DMartin
 * This class extends the generic DAO interface to define the queries
 * associated with a supplier content set.
 */
public interface ContentSetDAO {

    String GET_SUPPLIER_BY_NAME = "getSupplierByName";
    String GET_SUPPLIER_SETS = "getSupplierSets";

    /**
     * Query interface to return a single supplier content set by its name.
     * @param name - The logical name of the content set.
     * @return - A single consumer content set whose logical name matches the parameter 'name'.
     */
    public ContentSet getSupplierByName(String name);

    /**
     * Query interface to return a list of all supplier content sets.
     * @return - A complete list of supplier content sets.
     */
    public List<ContentSet> getSupplierSets();
}
