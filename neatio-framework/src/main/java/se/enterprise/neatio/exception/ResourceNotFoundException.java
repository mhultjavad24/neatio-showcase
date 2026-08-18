package se.enterprise.neatio.exception;

import java.net.URI;

/**
 * Exception thrown when a requested resource or entity cannot be found (HTTP 404).
 */
public class ResourceNotFoundException extends NeatioException {

    private static final int STATUS_NOT_FOUND = 404;
    private static final String DEFAULT_TYPE = "https://neatio.internal/errors/resource-not-found";
    private static final String DEFAULT_TITLE = "Resource Not Found";

    public ResourceNotFoundException(String detail) {
        super(STATUS_NOT_FOUND, DEFAULT_TYPE, DEFAULT_TITLE, detail);
    }

    public ResourceNotFoundException(String type, String title, String detail) {
        super(STATUS_NOT_FOUND, type, title, detail);
    }

    public ResourceNotFoundException(URI type, String title, String detail) {
        super(STATUS_NOT_FOUND, type, title, detail);
    }
}
