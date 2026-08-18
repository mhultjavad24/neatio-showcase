package se.enterprise.neatio.model;

/**
 * Represents a single field validation failure in an RFC 7807 problem details response.
 *
 * @param field         the name of the invalid property/field
 * @param rejectedValue the invalid value provided by client
 * @param message       the localized validation error message
 */
public record InvalidParameter(
    String field,
    Object rejectedValue,
    String message
) {}
