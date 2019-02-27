package mil.nga.efd.validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import mil.nga.efd.domain.ScheduleExpression;

/**
 * Implementation class for the schedule duration validator.  This class
 * contains the logic to validate that a ScheduleExpression object contains
 * a valid duration.
 * 
 * @author L. Craig Carpenter
 */
public class ValidScheduleDurationValidator
		implements ConstraintValidator<ValidScheduleDuration, ScheduleExpression> {
    
    /**
     * This <code>isValid</code> method will determine whether the incoming 
     * <code>ScheduleExpression</code> conforms to a valid expression.  
     * 
     * @param expression The <code>ScheduleExpression</code> object to validate.
     * @param unused Unused
     * @return True if the expression is valid, false otherwise.
     */
	@Override
	public boolean isValid(ScheduleExpression expression, 
			ConstraintValidatorContext unused) {
		boolean valid = false;
		if (expression != null) {
			if ((expression.getDuration() > 0) &&
					(expression.getDurationUnit() == null)) {
				valid = false;
			}
			else {
				valid = true;
			}
		}
		return valid;
	}

}
