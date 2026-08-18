package se.enterprise.neatio.annotation;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or class for enterprise operational auditing.
 * Records execution timing, correlation ID, method arguments, and outcomes.
 */
@Inherited
@Documented
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface NeatioAudited {

    /**
     * Business action code or name (e.g. "KUDOS_TRANSFER", "FIKA_REDEEM").
     */
    @Nonbinding
    String action() default "";

    /**
     * Whether to capture method input parameters in the audit record.
     */
    @Nonbinding
    boolean logParameters() default true;

    /**
     * Whether to capture method return value in the audit record.
     */
    @Nonbinding
    boolean logResult() default true;
}
