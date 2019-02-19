package mil.nga.efd.controllers;

import mil.nga.efd.domain.PendingDelete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Completely re-wrote to remove dependencies on outdated Hibernate.
 * 
 * @author L. Craig Carpenter
 */
public class PendingDeleteDAOHibernate implements PendingDeleteDAO {

	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		PendingDeleteDAOHibernate.class);
    
	/**
	 * Inject the EntityManager object.
	 */
	@Autowired
	private EntityManager em;

	/**
	 * Default no-arg constructor.
	 */
	public PendingDeleteDAOHibernate() {}
	
    @SuppressWarnings("unchecked")
	public PendingDelete getByPath(Long consumerContentSetId, String path) {
    	PendingDelete pDelete = null;
        
        if (em != null) {
        	
    		CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<PendingDelete> cq = cb.createQuery(PendingDelete.class);
            Root<PendingDelete> root = cq.from(PendingDelete.class);
            
            // Add the "where" clause
            cq.where(
                cb.and(
                    cb.equal(
                        root.get("consumerId"), 
                        cb.parameter(Long.class, "consumerId")),
                    cb.equal(
                    	root.get("path"), 
                        cb.parameter(String.class, "path"))
                )
            );
            
            // Create the query
            Query query = em.createQuery(cq);
            
            // Set the value for the where clause
            query.setParameter("consumerId", consumerContentSetId);
            query.setParameter("path", path);
            
            List<PendingDelete> results = query.getResultList();
            if (!results.isEmpty()) {
        		pDelete = results.get(0);
            	if (LOGGER.isDebugEnabled()) { 
            		LOGGER.debug("Found PendingDelete record with path => [ "
            				+ path
            				+ " ] and consumer content set ID => [ "
            				+ consumerContentSetId
            				+ " ].");
            	}
            }
            else {
            	if (LOGGER.isDebugEnabled()) { 
            		LOGGER.debug("No results obtained in search for "
            				+ "PendingDelete record by path.");
            	}
            }
        }
    	else {
    		LOGGER.error("The EntityManager object was not injected.  Unable "
    				+ "to connect to the target database.");
    	}
        return pDelete;
    }

    @SuppressWarnings("unchecked")
	public PendingDelete getNextToDelete(
    		Long consumerContentSetId, 
    		Calendar date) {
    	
        PendingDelete pDelete = null;
        
        if (em != null) {
        	List<PendingDelete> results = em.createNamedQuery(GET_NEXT_TO_DELETE)
        			.setParameter("consumerId", consumerContentSetId)
        			.setParameter("adjustedTime", date)
        			.getResultList();
        	if (!results.isEmpty()) {
        		pDelete = results.get(0);
        		if (LOGGER.isDebugEnabled()) {
        			LOGGER.debug("Next to delete => [ " 
        					+ pDelete.getPath()
        					+ " ].");
        		}
            }
            else {
            	if (LOGGER.isDebugEnabled()) { 
            		LOGGER.debug("No results obtained in search for "
            				+ "next PendingDelete record to remove.");
            	}
            }
        }
    	else {
    		LOGGER.error("The EntityManager object was not injected.  Unable "
    				+ "to connect to the target database.");
    	}
        return pDelete;
    }

}
