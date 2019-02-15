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

import org.springframework.beans.factory.FactoryBean;

import com.solers.util.unit.TimeIntervalUnit;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class TimeIntervalUnitFactoryBean implements FactoryBean {

    private long value;
    private TimeIntervalUnit sourceUnit;
    private TimeIntervalUnit destUnit = TimeIntervalUnit.MILLISECONDS;
    
    public void setValue(long value) {
        this.value = value;
    }

    public void setSourceUnit(TimeIntervalUnit sourceUnit) {
        this.sourceUnit = sourceUnit;
    }

    public void setDestUnit(TimeIntervalUnit destUnit) {
        this.destUnit = destUnit;
    }

    @Override
    public Object getObject() throws Exception {
        return destUnit.convert(value, sourceUnit);
    }

    @Override
    public Class<?> getObjectType() {
        return long.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
