package se.enterprise.neatio.interceptor;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.enterprise.neatio.annotation.NeatioAudited;
import se.enterprise.neatio.filter.CorrelationContext;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;

/**
 * Interceptor that produces structured audit logs for operations annotated with @NeatioAudited.
 */
@Interceptor
@NeatioAudited
@Priority(Interceptor.Priority.APPLICATION)
public class NeatioAuditInterceptor {

    private static final Logger log = LoggerFactory.getLogger(NeatioAuditInterceptor.class);

    @Inject
    private CorrelationContext correlationContext;

    @AroundInvoke
    public Object auditOperation(InvocationContext ctx) throws Exception {
        Method method = ctx.getMethod();
        NeatioAudited config = findAnnotation(method, ctx.getTarget().getClass());

        String action = resolveActionName(config, method);
        String correlationId = correlationContext != null ? correlationContext.getCorrelationId() : "N/A";
        Instant timestamp = Instant.now();
        long startNs = System.nanoTime();

        String targetName = ctx.getTarget().getClass().getSimpleName() + "#" + method.getName();
        String paramsStr = (config != null && config.logParameters()) ? Arrays.deepToString(ctx.getParameters()) : "[MASKED]";

        try {
            Object result = ctx.proceed();
            long durationMs = (System.nanoTime() - startNs) / 1_000_000;

            String resultStr = (config != null && config.logResult() && result != null) ? result.toString() : "[SUPPRESSED]";

            log.info("[AUDIT_SUCCESS] action={} correlationId={} target={} durationMs={} timestamp={} params={} result={}",
                    action, correlationId, targetName, durationMs, timestamp, paramsStr, resultStr);

            return result;
        } catch (Exception ex) {
            long durationMs = (System.nanoTime() - startNs) / 1_000_000;

            log.warn("[AUDIT_FAILED] action={} correlationId={} target={} durationMs={} timestamp={} params={} error={}",
                    action, correlationId, targetName, durationMs, timestamp, paramsStr, ex.getMessage());

            throw ex;
        }
    }

    private NeatioAudited findAnnotation(Method method, Class<?> targetClass) {
        if (method != null && method.isAnnotationPresent(NeatioAudited.class)) {
            return method.getAnnotation(NeatioAudited.class);
        }
        if (targetClass != null && targetClass.isAnnotationPresent(NeatioAudited.class)) {
            return targetClass.getAnnotation(NeatioAudited.class);
        }
        return null;
    }

    private String resolveActionName(NeatioAudited annotation, Method method) {
        if (annotation != null && !annotation.action().isBlank()) {
            return annotation.action();
        }
        return method != null ? method.getName().toUpperCase() : "UNKNOWN_ACTION";
    }
}
