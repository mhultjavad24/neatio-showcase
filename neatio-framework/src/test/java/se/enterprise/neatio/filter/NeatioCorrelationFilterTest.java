package se.enterprise.neatio.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NeatioCorrelationFilterTest {

    private NeatioCorrelationFilter filter;
    private CorrelationContext correlationContext;
    private ContainerRequestContext requestContext;
    private ContainerResponseContext responseContext;

    @BeforeEach
    void setUp() {
        filter = new NeatioCorrelationFilter();
        correlationContext = new CorrelationContext();
        requestContext = mock(ContainerRequestContext.class);
        responseContext = mock(ContainerResponseContext.class);

        // Inject correlationContext via reflection
        try {
            var field = NeatioCorrelationFilter.class.getDeclaredField("correlationContext");
            field.setAccessible(true);
            field.set(filter, correlationContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldPropagateExistingCorrelationId() throws IOException {
        String existingCid = "existing-test-cid-123";
        when(requestContext.getHeaderString(NeatioCorrelationFilter.HEADER_NAME)).thenReturn(existingCid);

        filter.filter(requestContext);

        verify(requestContext).setProperty(NeatioCorrelationFilter.PROPERTY_KEY, existingCid);
        assertThat(correlationContext.getCorrelationId()).isEqualTo(existingCid);
        assertThat(MDC.get(NeatioCorrelationFilter.MDC_KEY)).isEqualTo(existingCid);
    }

    @Test
    void shouldGenerateNewCorrelationIdWhenMissing() throws IOException {
        when(requestContext.getHeaderString(NeatioCorrelationFilter.HEADER_NAME)).thenReturn(null);

        filter.filter(requestContext);

        ArgumentCaptor<String> cidCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestContext).setProperty(eq(NeatioCorrelationFilter.PROPERTY_KEY), cidCaptor.capture());

        String generatedCid = cidCaptor.getValue();
        assertThat(generatedCid).isNotBlank();
        assertThat(correlationContext.getCorrelationId()).isEqualTo(generatedCid);
        assertThat(MDC.get(NeatioCorrelationFilter.MDC_KEY)).isEqualTo(generatedCid);
    }

    @Test
    void shouldSetResponseHeaderAndCleanMdc() throws IOException {
        String cid = "response-test-cid-456";
        when(requestContext.getProperty(NeatioCorrelationFilter.PROPERTY_KEY)).thenReturn(cid);

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);

        MDC.put(NeatioCorrelationFilter.MDC_KEY, cid);
        filter.filter(requestContext, responseContext);

        assertThat(headers.getFirst(NeatioCorrelationFilter.HEADER_NAME)).isEqualTo(cid);
        assertThat(MDC.get(NeatioCorrelationFilter.MDC_KEY)).isNull();
    }
}
