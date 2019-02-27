package mil.nga.efd.validations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Annotation interface for validating that a file falls underneath the 
 * EFD home directory.
 * 
 * TODO: This validator will need to be revisited when beginning code changes
 * to handle S3.
 * 
 * @author L. Craig Carpenter
 */
@Constraint(validatedBy = RestrictedPathValidator.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestrictedPath {
    
    String message() default "{pathRestricted}";
    
    Class<?>[] groups() default{};
    
    Class<? extends Payload>[] payload() default{};
}
