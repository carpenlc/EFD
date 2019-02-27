package mil.nga.efd.controllers;

import java.util.Collection;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import mil.nga.efd.domain.Alert;
import mil.nga.efd.domain.Alert.AlertType;
import com.solers.util.Page;

/**
 * Completely re-wrote to remove dependencies on outdated Hibernate.
 * 
 * TODO: LCC - This class has some squirrelly logic allowing callers to 
 * restrict the results based on rownum.  Revisit later to see if we 
 * actually need this logic for anything.  The code is left in but 
 * commented out.
 * 
 * @author L. Craig Carpenter
 */
@Repository
public class AlertDAOHibernate 
		extends GenericHibernateDAO<Alert, Long> implements AlertDAO {
	
	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		AlertDAOHibernate.class);
	
	/**
	 * Default no-arg constructor.
	 */
	public AlertDAOHibernate() {}
	
	/**
	 * List alerts by the alert type.
	 */
    @Override
    @SuppressWarnings("unchecked")
    public Page<Alert> listBy(AlertType type, int startIndex, int max) {
    	
    	Page<Alert> alerts = null;
    	
    	// Ensure the EntityManager is injected before proceeding
    	if (em != null) {
    		
    		
    		CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Alert> cq = cb.createQuery(Alert.class);
            Root<Alert> root = cq.from(Alert.class);
            
            // Add the "where" clause
            cq.where(
                cb.or(
                    cb.equal(
                        root.get("type"), 
                        cb.parameter(String.class, "type1")),
                    cb.equal(
                    	root.get("type"), 
                        cb.parameter(String.class, "type2"))
                )
            );
            
            // Add the "order by" clause
            cq.orderBy(cb.desc(root.get("timestamp")));
            
            // Create the query
            Query query = em.createQuery(cq);
            
            // Set the value for the where clause
            query.setParameter("type1", type);
            query.setParameter("type2", AlertType.ALL);
            
            List<Alert> alertList = query.getResultList();
            alerts = new Page<Alert>(alertList);
    		
    	}
    	else {
    		LOGGER.error("The EntityManager object was not injected.  Unable "
    				+ "to connect to the target database.");
    	}
    	return alerts;

        /*
        Integer count = (Integer) q.uniqueResult();
        
        // Derby does not support "order by" in subqueries so we have to 
        // select from the end of the result set.
        int ascendingEnd = startIndex + max;
        int descendingStart = count - ascendingEnd;
        int descendingEnd = count - startIndex;
        
        if (descendingStart < 0) {
            descendingStart = 0;
        }
        
        q = getSession().createSQLQuery(
        		"select * from (select row_number() over() as rownum, alerts.* "
        		+ "from alerts where type = :type or type = :all) as "
        		+ "tmp where rownum > :start and rownum <= :end order by timestamp desc");
        q.setParameter("type", type.ordinal());
        q.setParameter("all", AlertType.ALL.ordinal());
        q.setParameter("start", descendingStart);
        q.setParameter("end", descendingEnd);
        q.addEntity(Alert.class);
        
        List<Alert> list = (List<Alert>) q.list();
        return new Page<Alert>(count, list);
        */
    }

    /**
     * 
     */
    public List<Alert> findAll() {
    	
    	List<Alert> results = null;
    	
    	if (em != null) {
    		CriteriaBuilder cb = em.getCriteriaBuilder();
    		CriteriaQuery<Alert> cq = cb.createQuery(Alert.class);
    		Root<Alert> root = cq.from(Alert.class);
    		CriteriaQuery<Alert> all = cq.select(root);
    		TypedQuery<Alert> allQuery = em.createQuery(all);
    		results = allQuery.getResultList();
    	}
    	else {
    		LOGGER.error("The EntityManager object was not injected.  Unable "
				+ "to connect to the target database.  No records selected "
    			+ "from the Alert table.");
    	}
    	return results;
    }
    /**
     * Remove a list of IDs from the target data source.
     * 
     * @param ids A Collection of IDs to remove from the data source.
     */
    @Override
    public void makeTransientById(Collection<Long> ids) {
    	
    	int result = 0;
    	
    	if (em != null) {
    		if ((ids != null) && (ids.size() > 0)) {
		    	
    			CriteriaBuilder cb  = em.getCriteriaBuilder();
		    	CriteriaDelete<Alert> cd = cb.createCriteriaDelete(Alert.class);
		    	Root<Alert> root = cd.from(Alert.class);
		    	cd.where(root.get("id").in(ids));
		
		    	result = em.createQuery(cd).executeUpdate();
		    	
		    	if (LOGGER.isDebugEnabled()) {
		    		LOGGER.debug("Removed [ " + result + " ] alert records.");
		    	}  
    		}
    		else {
    			LOGGER.info("No Alert IDs identified for delete.");
    		}
    	}
    	else {
    		LOGGER.error("The EntityManager object was not injected.  Unable "
				+ "to connect to the target database.  No records will be "
    			+ "removed from the Alert table.");
    	}
    }

}
