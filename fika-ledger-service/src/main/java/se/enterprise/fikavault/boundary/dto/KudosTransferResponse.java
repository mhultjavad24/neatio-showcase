package se.enterprise.fikavault.boundary.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Receipt returned upon successful Kudos transfer completion.
 */
public record KudosTransferResponse(
    UUID transactionId,
    String status,
    Instant timestamp
) {}
