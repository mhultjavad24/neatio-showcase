package se.enterprise.fikavault.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Historical transaction entry in the employee ledger.
 */
public record TransactionRecord(
    UUID id,
    String type,
    String senderId,
    String recipientId,
    int amount,
    String description,
    Instant timestamp
) {}
