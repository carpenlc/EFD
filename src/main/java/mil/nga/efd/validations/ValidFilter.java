package mil.nga.efd.validations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Annotation interface for validating that a filter is a valid 
 * regular expression (REGEX).
 * 
 * @author L. Craig Carpenter
 */
@Constraint(validatedBy = ValidFilterValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidFilter {
	
    String message() default "";
    
    Class<?>[] groups() default{};
    
    Class<? extends Payload>[] payload() default{};
}
