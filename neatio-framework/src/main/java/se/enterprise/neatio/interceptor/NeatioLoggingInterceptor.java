package se.enterprise.neatio.interceptor;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.enterprise.neatio.annotation.NeatioLogged;
import se.enterprise.neatio.filter.CorrelationContext;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Diagnostic method entry/exit and latency logging interceptor.
 */
@Interceptor
@NeatioLogged
@Priority(Interceptor.Priority.APPLICATION - 10)
public class NeatioLoggingInterceptor {

    private static final Logger log = LoggerFactory.getLogger(NeatioLoggingInterceptor.class);

    @Inject
    private CorrelationContext correlationContext;

    @AroundInvoke
    public Object logMethodInvocation(InvocationContext ctx) throws Exception {
        Method method = ctx.getMethod();
        String methodName = ctx.getTarget().getClass().getSimpleName() + "." + method.getName();
        String correlationId = correlationContext != null ? correlationContext.getCorrelationId() : "N/A";

        if (log.isDebugEnabled()) {
            log.debug("[ENTER] [{}] {} args={}", correlationId, methodName, Arrays.deepToString(ctx.getParameters()));
        }

        long startNs = System.nanoTime();
        try {
            Object result = ctx.proceed();
            long durationMs = (System.nanoTime() - startNs) / 1_000_000;

            if (log.isDebugEnabled()) {
                log.debug("[EXIT] [{}] {} completed in {}ms", correlationId, methodName, durationMs);
            }
            return result;
        } catch (Exception ex) {
            long durationMs = (System.nanoTime() - startNs) / 1_000_000;
            log.warn("[EXCEPTION] [{}] {} failed after {}ms: {}", correlationId, methodName, durationMs, ex.getMessage());
            throw ex;
        }
    }
}
