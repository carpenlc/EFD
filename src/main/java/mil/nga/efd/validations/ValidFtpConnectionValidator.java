package mil.nga.efd.validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import mil.nga.efd.domain.ContentSet;

/**
 * Implementation class for the FTP connection validator.  This class
 * contains the logic to validate that a FTP connection is available.
 * 
 * @author L. Craig Carpenter
 */
public class ValidFtpConnectionValidator
		implements ConstraintValidator<ValidFtpConnection, ContentSet> {

    /**
     * This <code>isValid</code> method will determine whether the FTP 
     * connection associated with GBS transport is valid.
     * 
     * @param filter The <code>ContentSet</code> object to validate.
     * @param unused Unused
     * @return True if the FTP connection is valid, false otherwise.
     */
	@Override
	public boolean isValid(ContentSet cs, ConstraintValidatorContext unused) {
		boolean valid = false;
		if (cs != null) {
			if (cs.isSupplier()) {
				if (cs.isSupportsGbsTransport()) {
					valid = (cs.getFtpConnection() != null);
				}
				else {
					valid = (cs.getFtpConnection() != null);
				}
			}
		}
		return valid;
	}

}
