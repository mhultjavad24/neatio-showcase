package se.enterprise.neatio.exception;

import java.net.URI;

/**
 * Exception thrown when a business invariant or domain rule is violated (HTTP 422).
 */
public class BusinessRuleException extends NeatioException {

    private static final int STATUS_UNPROCESSABLE_ENTITY = 422;
    private static final String DEFAULT_TYPE = "https://neatio.internal/errors/business-rule-violation";
    private static final String DEFAULT_TITLE = "Business Rule Violation";

    public BusinessRuleException(String detail) {
        super(STATUS_UNPROCESSABLE_ENTITY, DEFAULT_TYPE, DEFAULT_TITLE, detail);
    }

    public BusinessRuleException(String type, String title, String detail) {
        super(STATUS_UNPROCESSABLE_ENTITY, type, title, detail);
    }

    public BusinessRuleException(URI type, String title, String detail) {
        super(STATUS_UNPROCESSABLE_ENTITY, type, title, detail);
    }
}
