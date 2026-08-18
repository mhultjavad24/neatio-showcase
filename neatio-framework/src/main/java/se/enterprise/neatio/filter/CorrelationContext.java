package se.enterprise.neatio.filter;

import jakarta.enterprise.context.RequestScoped;
import org.slf4j.MDC;

import java.io.Serializable;
import java.util.UUID;

/**
 * Request-scoped contextual holder for the active correlation identifier.
 */
@RequestScoped
public class CorrelationContext implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String MDC_KEY = "correlationId";
    public static final String HEADER_NAME = "X-Correlation-ID";

    private String correlationId;

    public String getCorrelationId() {
        if (correlationId == null) {
            String mdcVal = MDC.get(MDC_KEY);
            if (mdcVal != null && !mdcVal.isBlank()) {
                this.correlationId = mdcVal;
            } else {
                this.correlationId = UUID.randomUUID().toString();
                MDC.put(MDC_KEY, this.correlationId);
            }
        }
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        if (correlationId != null) {
            MDC.put(MDC_KEY, correlationId);
        } else {
            MDC.remove(MDC_KEY);
        }
    }
}
