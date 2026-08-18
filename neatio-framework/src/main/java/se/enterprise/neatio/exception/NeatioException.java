package se.enterprise.neatio.exception;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base enterprise runtime exception carrying RFC 7807 problem details semantics.
 */
public class NeatioException extends RuntimeException {

    private final int status;
    private final URI type;
    private final String title;
    private final String detail;
    private final Map<String, Object> extensions;

    public NeatioException(int status, URI type, String title, String detail) {
        super(detail != null ? detail : title);
        this.status = status;
        this.type = type;
        this.title = title;
        this.detail = detail;
        this.extensions = new LinkedHashMap<>();
    }

    public NeatioException(int status, String type, String title, String detail) {
        this(status, type != null ? URI.create(type) : null, title, detail);
    }

    public NeatioException(int status, String title, String detail) {
        this(status, (URI) null, title, detail);
    }

    public int getStatus() {
        return status;
    }

    public URI getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public Map<String, Object> getExtensions() {
        return Collections.unmodifiableMap(extensions);
    }

    public NeatioException withExtension(String key, Object value) {
        this.extensions.put(key, value);
        return this;
    }
}
