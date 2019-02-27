package mil.nga.efd.validations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Annotation interface for validating that a file path is valid.
 * 
 * @author L. Craig Carpenter
 */
@Constraint(validatedBy = ValidFileNameValidator.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidFileName {
    
    String message() default "{pathInvalidChars}";
    
    Class<?>[] groups() default{};
    
    Class<? extends Payload>[] payload() default{};
}
