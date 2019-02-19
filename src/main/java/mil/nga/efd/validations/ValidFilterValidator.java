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
import javax.validation.Validator;

import mil.nga.efd.domain.FileFilter;
import mil.nga.efd.validations.ValidFilter;

public class ValidFilterValidator implements ConstraintValidator<ValidFilter, FileFilter> {
	
    @Override
    public void initialize(ValidFilter arg0) {
    
    }

    @Override
    public boolean isValid(Object arg) {
        if (arg == null) return false;
        if (!(arg instanceof FileFilter)) return false;
        FileFilter f = (FileFilter) arg;
        if (f.getPatternType() == null || f.getPattern() == null) return false;
        if (f.getPatternType().equals(FileFilter.Pattern.REGEX)) {
            try {
                Pattern.compile(f.getPattern());
            } catch (PatternSyntaxException pse) {
                return false;
            }
        }
        return true;
    }
}
