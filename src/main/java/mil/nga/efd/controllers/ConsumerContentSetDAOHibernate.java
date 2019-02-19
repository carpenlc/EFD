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
import org.springframework.beans.factory.annotation.Autowired;

import mil.nga.efd.domain.ConsumerContentSet;


/**
 * @author DMartin
 * This class extends the generic DAO operations to provide methods for each of the named queries
 * associated with a consumer content set. The queries are performed via hibernate queries.
 */
public class ConsumerContentSetDAOHibernate implements ConsumerContentSetDAO {

	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		ConsumerContentSetDAOHibernate.class);
    
	/**
	 * Inject the EntityManager object.
	 */
	@Autowired
	private EntityManager em;
	
    /**
     * Perform hibernate query on the consumer_set_info and content_set tables.
     * @param name - The logical name of the content set.
     * @return - A single consumer content set whose logical name matches the parameter 'name'.
     */
    @SuppressWarnings("unchecked")
	public ConsumerContentSet getConsumerByName(String name) {
        ConsumerContentSet result = null;
        if (em != null) {
        	List<ConsumerContentSet> results = em.createNamedQuery(GET_CONSUMER_BY_NAME)
        			.setParameter("name", name)
        			.getResultList();
        	if (!results.isEmpty()) {
        		result = results.get(0);
        		if (LOGGER.isDebugEnabled()) {
        			LOGGER.debug("Found ConsumerContentSet by name => [ " 
        					+ name
        					+ " ].");
        		}
            }
            else {
            	if (LOGGER.isDebugEnabled()) { 
            		LOGGER.debug("No results obtained in search for "
            				+ "ConsumerContentSet by name => [ "
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
     * Perform hibernate query on the consumer_set_info and content_set tables.
     * @return A complete list of consumer content sets.
     */
    @SuppressWarnings("unchecked")
    public List<ConsumerContentSet> getConsumerSets() {
    	List<ConsumerContentSet> results = null;
        if (em != null) {
        	results = em.createNamedQuery(GET_CONSUMER_SETS)
        			.getResultList();
        }
    	else {
    		LOGGER.error("The EntityManager object was not injected.  Unable "
    				+ "to connect to the target database.");
    	}
        return results;
    }
}
