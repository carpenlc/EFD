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
package mil.nga.efd.validations;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import mil.nga.efd.domain.FileFilter;

/**
 * Implementation class for the filter validator.  This class
 * contains the logic to validate that a String is a valid regular expression 
 * (REGEX).
 * 
 * @author L. Craig Carpenter
 */
public class ValidFilterValidator 
	implements ConstraintValidator<ValidFilter, FileFilter> {
	
    /**
     * This <code>isValid</code> method will determine whether the filter 
     * contains a valid regular expression (REGEX).
     * 
     * @param filter The <code>FileFilter</code> object to validate.
     * @param unused Unused
     * @return True if the filter is valid, false otherwise.
     */
	@Override
	public boolean isValid(FileFilter filter, ConstraintValidatorContext unused) {
		boolean valid = false;
		if (filter != null) {
            if ((filter.getPatternType() != null) && 
            	(filter.getPattern() != null)) {
            	if (filter.getPatternType().equals(FileFilter.Pattern.REGEX)) {
            		try {
                        Pattern.compile(filter.getPattern());
                        valid = true;
                    } catch (PatternSyntaxException pse) {
                        valid = false;
                    }
            	}
            }
		}
		return valid;
	}
}
