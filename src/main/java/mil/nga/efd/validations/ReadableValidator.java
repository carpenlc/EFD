package mil.nga.efd.validations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Implementation class for validating whether an input file is readable.  
 * 
 * @author L. Craig Carpenter
 */
public class ReadableValidator 
		implements ConstraintValidator<Readable, String> {

	/**
	 * This method assumes the input is a String that defines the path to 
	 * a target file or directory.  It then checks to see if the target 
	 * location is readable. 
	 * 
	 * @param path A string representing a path to validate.
     * @param notused Not used.
	 * @return True if the target is readable, false otherwise.
	 */
	@Override
	public boolean isValid(String path, ConstraintValidatorContext notused) {
		boolean valid = false;
		if ((path != null) && (!path.isEmpty())) {
			Path p = Paths.get(path);
			valid = Files.isReadable(p);
		}
		return valid;
	}
}
