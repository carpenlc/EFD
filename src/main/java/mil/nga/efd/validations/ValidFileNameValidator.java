package mil.nga.efd.validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.solers.delivery.util.FileSystemUtil;

/**
 * Implementation class for the filename validator.  This class
 * contains the logic to validate that a String is a valid file name.
 * 
 * @author L. Craig Carpenter
 */
public class ValidFileNameValidator 
		implements ConstraintValidator<ValidFileName, String> {
	
    /**
     * This <code>isValid</code> method will determine whether input String
     * is a valid file name.  
     * 
     * @param filter The <code>String</code> path to validate.
     * @param unused Unused
     * @return True if the filename is valid, false otherwise.
     */
	@Override
	public boolean isValid(String path, ConstraintValidatorContext unused) {
		boolean valid = false;
		if (path != null) {
			valid = FileSystemUtil.isFilenameValid(path);
		}
		return valid;
	}
}
