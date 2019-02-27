package mil.nga.efd.validations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Annotation interface for validating that an FTP connection is valid.
 * 
 * @author L. Craig Carpenter
 */
@Constraint(validatedBy = ValidFtpConnectionValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidFtpConnection {
    
    String message() default "{contentset.ftpconnection.required}";
    
    Class<?>[] groups() default{};
    
    Class<? extends Payload>[] payload() default{};
}
