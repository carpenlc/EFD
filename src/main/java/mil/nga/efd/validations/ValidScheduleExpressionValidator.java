package mil.nga.efd.validations;

import java.text.ParseException;
import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation class for the schedule expression validator.  This class
 * contains the logic to validate that a String is a valid cron expression.
 * 
 * @author L. Craig Carpenter
 */
public class ValidScheduleExpressionValidator 
		implements ConstraintValidator<ValidScheduleExpression, String> {
  
	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		ValidScheduleExpressionValidator.class);
   
    /**
     * This <code>isValid</code> method will determine whether the incoming 
     * String conforms to a valid cron expression.  
     * 
     * @param expression The String object to validate.
     * @param unused Unused
     * @return True if the expression is valid, false otherwise.
     */
	@Override
	public boolean isValid(String expression, ConstraintValidatorContext unused) {
		boolean valid = false;
		try {
            CronExpression cronExpr = new CronExpression(expression);
            cronExpr.getNextValidTimeAfter(new Date());
            valid = true;
        } catch (ParseException ex) {
            LOGGER.error("Invalid cron expression => [ "
            		+ expression
            		+ " ].");
        } catch (UnsupportedOperationException ex) {
        	LOGGER.error("UnsupportedOperationException while validating "
        			+ "cron expression => [ "
        			+ expression
        			+ " ], Exception message => [ "
        			+ ex.getMessage()
        			+ " ].");
        }
		
		return valid;
	}
}