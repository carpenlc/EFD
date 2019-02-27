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
package mil.nga.efd.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.validation.constraints.NotEmpty;

import mil.nga.efd.validations.ValidScheduleDuration;
import mil.nga.efd.validations.ValidScheduleExpression;
import com.solers.util.unit.TimeIntervalUnit;

@Entity
@Table(name = "schedule_expression")
@ValidScheduleDuration
public class ScheduleExpression implements Serializable {

	/**
	 * Eclipse-generated serialVersionUID
	 */
	private static final long serialVersionUID = 4534343899371864583L;

	private static final int EXPRESSION_LEN = 255;

    private Long id;
    private String cronExpression;
    private int duration;
    private TimeIntervalUnit durationUnit;
    
    /**
     * Default no-arg constructor
     */
    public ScheduleExpression() {  }
    
    public ScheduleExpression (String cronExpression) {
       this.cronExpression = cronExpression;	
    }
    
    @Id
    @Column(nullable=false, updatable=false, insertable=false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
    	return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(nullable=false, updatable=true, length=EXPRESSION_LEN)
    @NotEmpty(message="{scheduleexpression.cronexpression.required}")
    @ValidScheduleExpression
    public String getCronExpression() {
    	return cronExpression;
    }
        
    public void setCronExpression(String expression) {
    	this.cronExpression = expression;
    }
    
    @Column(name = "duration", nullable = true)
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Column(name = "durationunit", nullable = true)
    public TimeIntervalUnit getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(TimeIntervalUnit durationUnit) {
        this.durationUnit = durationUnit;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScheduleExpression) {
            if (cronExpression == null && ((ScheduleExpression)obj).cronExpression == null) {
                return true;
            }
            
            if (cronExpression == null ^ ((ScheduleExpression)obj).cronExpression == null) {
                return false;
            }
            
            return ((ScheduleExpression) obj).cronExpression.equals(cronExpression);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return cronExpression == null ? 0 : cronExpression.hashCode();
    }
    
    @Override
    public String toString() {
        return new StringBuilder("id:").append(id)
                              .append(",expression:").append(cronExpression)
                              .append(",duration:").append(duration)
                              .append(",durationUnit:").append(durationUnit)
                              .toString();
    }
}