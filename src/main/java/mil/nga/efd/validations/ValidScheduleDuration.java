package mil.nga.efd.validations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import mil.nga.efd.validations.ValidScheduleDurationValidator;

/**
 * Annotation interface for validating that an expression is a valid 
 * schedule duration.  This is ultimately used for scheduling via Quartz.
 * 
 * @author L. Craig Carpenter
 */
@Constraint(validatedBy = ValidScheduleDurationValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidScheduleDuration {
    
    String message() default "{scheduleexpression.duration.invalid}";
    
    Class<?>[] groups() default{};
    
    Class<? extends Payload>[] payload() default{};
}
