package mil.nga.efd.validations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation class checking to see if a directory exists.  If the 
 * directory doesn't exist, the code will attempt to create it.  
 * 
 * @author L. Craig Carpenter
 */
public class ExistingDirectoryValidator 
		implements ConstraintValidator<ExistingDirectory, String> {

	/**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		ExistingDirectoryValidator.class);
    
    /**
     * This <code>isValid</code> method will ensure that a target 
     * directory exists.
     * 
     * @param filter The <code>String</code> path to validate.
     * @param notused not used
     * @return True if the directory path is valid, false otherwise.
     */
	@Override
	public boolean isValid(String path, ConstraintValidatorContext notused) {
		boolean valid = false;
		if ((path != null) && (!path.isEmpty())) {
			Path p = Paths.get(path);
			if (!Files.exists(p)) {
				try {
					Files.createDirectories(p);
					valid = true;
				}
				catch (IOException ioe) { 
					LOGGER.warn("IOException encountered while attempting to "
							+ "create directory [ "
							+ path
							+ " ].  Exception message => [ "
							+ ioe.getMessage()
							+ " ].");
				}
			}
			else {
				valid = true;
			}
		}
		return valid;
	}
}
