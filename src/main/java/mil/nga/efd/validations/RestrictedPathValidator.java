package mil.nga.efd.validations;

import java.io.File;
import java.io.IOException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solers.delivery.util.FileSystemUtil;

/**
 * Implementation class for the restricted path validator.  This class
 * contains the logic to determine whether or not a file falls underneath the
 * EFD file structure.
 * 
 * TODO: This validator will need to be revisited when beginning code changes
 * to handle S3.
 * 
 * @author L. Craig Carpenter
 */
public class RestrictedPathValidator 
		implements ConstraintValidator<RestrictedPath, File> {
    
	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		RestrictedPathValidator.class);

    /**
     * This <code>isValid</code> method will determine whether input File
     * falls within the EFD directories.  
     * 
     * @param filter The <code>File</code> to validate.
     * @param unused Unused
     * @return True if the incoming file falls underneath the EFD home
     * directory.
     */
	@Override
	public boolean isValid(File source, ConstraintValidatorContext arg1) {
		boolean valid = false;
		if (source != null) { 
	        try {
	            String canonicalPath = source.getCanonicalPath();
	            String efdPath = FileSystemUtil.getEFDHome().getCanonicalPath();
	            valid = FileSystemUtil.pathIsChild(canonicalPath, efdPath);
	        } catch (IOException ioe) {
	        	LOGGER.warn("Failed to canonicalize path.", ioe);
	            return true;
	        }
		}
		return valid;
	}
    
}
