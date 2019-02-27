package mil.nga.efd.validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.solers.delivery.transport.gbs.GBSConfigurator;

/**
 * Implementation class for determining whether or not GBS is enabled.  
 * 
 * @author L. Craig Carpenter
 */
public class GbsIsEnabledValidator 
		implements ConstraintValidator<GbsIsEnabled, Boolean> {

    /**
     * This <code>isValid</code> method checks to see if GBS is enabled.  
     * For the record, this method/class is absurd.
     * 
     * @param filter The Boolean object to evaluate
     * @param notused not used
     * @return True if GBS is enabled, false otherwise.
     */
	@Override
	public boolean isValid(Boolean gbsEnabled, ConstraintValidatorContext notused) {
		boolean valid = false;
		if (gbsEnabled != null) {
			if (gbsEnabled.booleanValue()) {
				valid = GBSConfigurator.isGBSEnabled();
			}
		}
		return valid;
	}
}
