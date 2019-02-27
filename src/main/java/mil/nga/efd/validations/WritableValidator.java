package mil.nga.efd.validations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * Class implementing a method to test whether a target String 
 * represents an on-disk location that is writable.  This class was 
 * completely re-written due to massive changes in the way validators
 * are now constructed.  
 * 
 * @author L. Craig Carpenter
 */
public class WritableValidator 
		implements ConstraintValidator<Writable, String> {

	/**
	 * This method assumes the input is a String that defines the path to 
	 * a target file or directory.  It then checks to see if the target 
	 * location is writable. 
	 * 
	 * @param path A string representing a path to validate.
     * @param unused Unused
	 * @return True if the target is writeable, false otherwise.
	 */
	@Override
	public boolean isValid(String path, ConstraintValidatorContext unused) {
		boolean valid = false;
		if ((path != null) && (!path.isEmpty())) {
			Path p = Paths.get(path);
			if (Files.exists(p)) {
				valid = Files.isWritable(p);
			}
			else {
				try {
					Files.createDirectories(p);
					valid = true;
				}
				catch (IOException ioe) { }
			}
		}
		return valid;
	}

}
