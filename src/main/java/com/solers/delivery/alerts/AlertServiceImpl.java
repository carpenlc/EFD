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
package com.solers.delivery.alerts;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import mil.nga.efd.controllers.AlertDAO;
import mil.nga.efd.controllers.AlertDAOHibernate;
//import com.solers.delivery.daos.DAOFactory;
import mil.nga.efd.domain.Alert;
import mil.nga.efd.domain.Alert.AlertType;
import com.solers.util.Page;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AlertServiceImpl implements AlertService {

	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		AlertServiceImpl.class);
    
	/**
	 * Inject the reference to the object responsible for interfacing with 
	 * the data source.
	 */
	@Autowired
	private AlertDAO alertDAO;
    
    /**
     * Default no-arg constructor
     */
    public AlertServiceImpl() { }

    @Override
    @Transactional
    public Page<Alert> list(AlertType type, int startIndex, int numRecords) {
       
    	Page<Alert> results = null;
    	
	    if (alertDAO != null) {
	    	results =  alertDAO.listBy(type, startIndex, numRecords);
		}
		else {
			LOGGER.error("The AlertDAO object was not injected.  Unable "
					+ "to connect to the target database.");
		}
        return results;
    }

    @Override
    @Transactional
    public void remove(Long id) {
	    if (alertDAO != null) {
	    	if (id != null) {
		    	Alert alert = alertDAO.findById(id, false);
		    	if (alert != null) {
		    		alertDAO.makeTransient(alert);
		    	}	
		    	else {
		    		LOGGER.warn("No Alert records found for ID [ "
		    				+ id
		    				+ " ].");
		    	}
	    	}
	    	else {
	    		LOGGER.warn("Input Alert ID was null.  Nothing to remove.");
	    	}
		}
		else {
			LOGGER.error("The AlertDAO object was not injected.  Unable "
					+ "to connect to the target database.");
		}
    }

    @Override
    @Transactional
    public void save(Alert alert) {
        AlertDAO dao = factory.getAlertDAO();
        dao.makePersistent(alert);
    }

    @Override
    @Transactional
    public Alert get(Long id) {
        AlertDAO dao = factory.getAlertDAO();
        return dao.getById(id);
    }

    @Override
    @Transactional
    public void remove(Collection<Long> ids) {
        AlertDAO dao = factory.getAlertDAO();
        dao.makeTransientById(ids);
    }
}
