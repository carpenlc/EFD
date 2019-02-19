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
package com.solers.delivery.content.consumer.difference;

/**
 * There are 3 types of ContentDifferences - adds, refreshes and deletes.
 * 
 * @author JGimourginas
 */
public enum ContentDifferenceActions {

    ADD ("ADD"),
    //would love to use UPDATE here instead of REFRESH, but the HSQL queries that take action type as a param don't
    //like the UPDATE keyword
    REFRESH  ("REFRESH"),
    REMOVE  ("REMOVE"),
    DONE ("DONE");

    private final String action;

    ContentDifferenceActions(String action) {
        this.action = action;
    }

    public String action() { return action; }

}
