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
 * Diagnostic method logging annotation for tracing entry, exit, and latency.
 */
@Inherited
@Documented
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface NeatioLogged {

    @Nonbinding
    boolean includeArguments() default true;

    @Nonbinding
    boolean includeResult() default false;
}
