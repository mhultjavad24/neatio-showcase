package se.enterprise.neatio.model;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of an RFC 7807 Problem Details object.
 */
public class ProblemDetail {

    private URI type;
    private String title;
    private int status;
    private String detail;
    private URI instance;
    private String correlationId;
    private Instant timestamp;
    private List<InvalidParameter> invalidParameters;
    private Map<String, Object> properties;

    public ProblemDetail() {
        this.timestamp = Instant.now();
    }

    public static ProblemDetail forStatus(int status) {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(status);
        return pd;
    }

    public static ProblemDetail forStatusAndDetail(int status, String detail) {
        ProblemDetail pd = forStatus(status);
        pd.setDetail(detail);
        return pd;
    }

    public static Builder builder() {
        return new Builder();
    }

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public URI getInstance() {
        return instance;
    }

    public void setInstance(URI instance) {
        this.instance = instance;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<InvalidParameter> getInvalidParameters() {
        return invalidParameters;
    }

    public void setInvalidParameters(List<InvalidParameter> invalidParameters) {
        this.invalidParameters = invalidParameters;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new LinkedHashMap<>();
        }
        this.properties.put(key, value);
    }

    public static class Builder {
        private final ProblemDetail problem = new ProblemDetail();

        public Builder type(URI type) {
            problem.setType(type);
            return this;
        }

        public Builder type(String type) {
            if (type != null) {
                problem.setType(URI.create(type));
            }
            return this;
        }

        public Builder title(String title) {
            problem.setTitle(title);
            return this;
        }

        public Builder status(int status) {
            problem.setStatus(status);
            return this;
        }

        public Builder detail(String detail) {
            problem.setDetail(detail);
            return this;
        }

        public Builder instance(URI instance) {
            problem.setInstance(instance);
            return this;
        }

        public Builder instance(String instance) {
            if (instance != null) {
                problem.setInstance(URI.create(instance));
            }
            return this;
        }

        public Builder correlationId(String correlationId) {
            problem.setCorrelationId(correlationId);
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            problem.setTimestamp(timestamp);
            return this;
        }

        public Builder invalidParameters(List<InvalidParameter> invalidParameters) {
            problem.setInvalidParameters(invalidParameters != null ? new ArrayList<>(invalidParameters) : null);
            return this;
        }

        public Builder addInvalidParameter(String field, Object rejectedValue, String message) {
            if (problem.getInvalidParameters() == null) {
                problem.setInvalidParameters(new ArrayList<>());
            }
            problem.getInvalidParameters().add(new InvalidParameter(field, rejectedValue, message));
            return this;
        }

        public Builder property(String key, Object value) {
            problem.setProperty(key, value);
            return this;
        }

        public ProblemDetail build() {
            return problem;
        }
    }
}
