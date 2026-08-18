package se.enterprise.neatio.filter;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * JAX-RS filter managing inbound and outbound X-Correlation-ID headers
 * and binding correlation tokens to SLF4J MDC.
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
@ApplicationScoped
public class NeatioCorrelationFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String HEADER_NAME = CorrelationContext.HEADER_NAME;
    public static final String MDC_KEY = CorrelationContext.MDC_KEY;
    public static final String PROPERTY_KEY = "se.enterprise.neatio.correlationId";

    @Inject
    private CorrelationContext correlationContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String inboundCid = requestContext.getHeaderString(HEADER_NAME);
        String correlationId = (inboundCid != null && !inboundCid.isBlank())
                ? inboundCid.trim()
                : UUID.randomUUID().toString();

        requestContext.setProperty(PROPERTY_KEY, correlationId);
        MDC.put(MDC_KEY, correlationId);

        if (correlationContext != null) {
            correlationContext.setCorrelationId(correlationId);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Object propCid = requestContext.getProperty(PROPERTY_KEY);
        String cid = propCid instanceof String ? (String) propCid : null;

        if (cid == null && correlationContext != null) {
            cid = correlationContext.getCorrelationId();
        }

        if (cid != null) {
            responseContext.getHeaders().putSingle(HEADER_NAME, cid);
        }

        MDC.remove(MDC_KEY);
    }
}
