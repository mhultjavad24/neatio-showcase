package se.enterprise.neatio.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationContextTest {

    private CorrelationContext context;

    @BeforeEach
    void setUp() {
        context = new CorrelationContext();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldSetAndGetCorrelationIdAndSyncWithMdc() {
        context.setCorrelationId("custom-cid-888");

        assertThat(context.getCorrelationId()).isEqualTo("custom-cid-888");
        assertThat(MDC.get(CorrelationContext.MDC_KEY)).isEqualTo("custom-cid-888");

        context.setCorrelationId(null);
        assertThat(MDC.get(CorrelationContext.MDC_KEY)).isNull();
    }

    @Test
    void shouldGenerateFallbackCorrelationIdWhenUnset() {
        String generatedCid = context.getCorrelationId();

        assertThat(generatedCid).isNotBlank();
        assertThat(MDC.get(CorrelationContext.MDC_KEY)).isEqualTo(generatedCid);
    }

    @Test
    void shouldPickUpMdcCorrelationIdIfAvailable() {
        MDC.put(CorrelationContext.MDC_KEY, "mdc-pre-existing-id");

        assertThat(context.getCorrelationId()).isEqualTo("mdc-pre-existing-id");
    }
}
