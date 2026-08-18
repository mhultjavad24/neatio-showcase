package se.enterprise.fikavault.api.dto;

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
