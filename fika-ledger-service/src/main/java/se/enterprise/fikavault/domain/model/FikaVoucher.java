package se.enterprise.fikavault.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an issued Fika voucher.
 */
public record FikaVoucher(
    UUID id,
    String employeeId,
    String itemDescription,
    String qrCodePayload,
    Instant issuedAt,
    Instant expiresAt,
    boolean redeemed
) {}
