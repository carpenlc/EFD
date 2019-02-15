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
package com.solers.util.spring;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Publishes all resolved properties to the System using {@code System.setProperty(String,String)}
 * 
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SystemPublishingPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private static final Logger log = Logger.getLogger(SystemPublishingPropertyPlaceholderConfigurer.class);
    
    @Override
    protected void processProperties(ConfigurableListableBeanFactory factory, Properties properties) throws BeansException {
        super.processProperties(factory, properties);
        
        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (System.getProperty(key) == null) {
                System.setProperty(key, properties.getProperty(key));
            } else {
               log.info(key+" already defined in system properties, skipping");
            }
        }
    }
}
