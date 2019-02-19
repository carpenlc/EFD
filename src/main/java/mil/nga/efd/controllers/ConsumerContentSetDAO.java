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

import mil.nga.efd.domain.ConsumerContentSet;
/**
 * @author DMartin
 * This class extends the generic DAO interface to define the queries
 * associated with a consumer content set.
 */
public interface ConsumerContentSetDAO {

    String GET_CONSUMER_BY_NAME = "getConsumerByName";
    String GET_CONSUMER_SETS = "getAllConsumers";

    /**
     * Query interface to return a single consumer content set by its name.
     * @param name - The logical name of the content set.
     * @return - A single consumer content set whose logical name matches the parameter 'name'.
     */
    public ConsumerContentSet getConsumerByName(String name);

    /**
     * Query interface to return a list of all consumer content sets.
     * @return - A complete list of consumer content sets.
     */
    public List<ConsumerContentSet> getConsumerSets();

}
