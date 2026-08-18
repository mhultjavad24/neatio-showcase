package se.enterprise.neatio.model;

import java.time.Instant;

/**
 * Structured audit record captured during audited business operations.
 */
public record AuditEvent(
    String action,
    String correlationId,
    Instant timestamp,
    long durationMs,
    String status,
    String targetClass,
    String targetMethod,
    String errorMessage
) {}
