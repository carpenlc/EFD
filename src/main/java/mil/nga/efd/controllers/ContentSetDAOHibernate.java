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

import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.efd.domain.ContentSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Completely re-wrote to remove dependencies on outdated Hibernate.  This 
 * class extends the generic DAO operations to provide methods for each of 
 * the named queries associated with a content set. The queries are
 * performed via hibernate queries.
 * 
 * @author L. Craig Carpenter
 */
@Transactional
public class ContentSetDAOHibernate implements ContentSetDAO {

	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		ContentSetDAOHibernate.class);
    
	/**
	 * Inject the EntityManager object.
	 */
	@Autowired
	private EntityManager em;
	
    /**
     * Perform hibernate query on the content_set table.
     * 
     * @param name -
     *            The logical name of the supplier content set.
     * @return - A single content set whose logical name matches the parameter 'name'.
     */
    @SuppressWarnings("unchecked")
	public ContentSet getSupplierByName(String name) {
    	
        ContentSet result = null;
        
        if (em != null) {
	    	List<ContentSet> results = em.createNamedQuery(GET_SUPPLIER_BY_NAME)
	    			.setParameter("name", name)
	    			.getResultList();
        	if (!results.isEmpty()) {
        		result = results.get(0);
        		if (LOGGER.isDebugEnabled()) {
        			LOGGER.debug("Found ContentSet by name => [ " 
        					+ name
        					+ " ].");
        		}
            }
        	else {
        		if (LOGGER.isDebugEnabled()) {
        			LOGGER.debug("Unable to find any ContentSet records by "
        					+ "name => [ " 
        					+ name
        					+ " ].");
        		}
        	}
        }
    	else {
    		LOGGER.error("The EntityManager object was not injected.  Unable "
    				+ "to connect to the target database.");
    	}
        return result;
    }

    /**
     * Perform hibernate query on the content_set table.
     * 
     * @return A complete list of supplier content sets.
     */
    @SuppressWarnings("unchecked")
    public List<ContentSet> getSupplierSets() {
        List<ContentSet> results = null;
        if (em != null) {
	    	results = em.createNamedQuery(GET_SUPPLIER_SETS)
	    			.getResultList();
	    	if (results.isEmpty()) {
        		if (LOGGER.isDebugEnabled()) {
        			LOGGER.debug("Unable to find any supplier sets.");
        		}
            }
        }
    	else {
    		LOGGER.error("The EntityManager object was not injected.  Unable "
    				+ "to connect to the target database.");
    	}
        return results;
    }
}
