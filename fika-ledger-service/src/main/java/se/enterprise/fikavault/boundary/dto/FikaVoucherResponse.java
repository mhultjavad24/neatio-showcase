package se.enterprise.fikavault.boundary.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Issued Fika voucher response containing café QR code payload and validity window.
 */
public record FikaVoucherResponse(
    UUID voucherId,
    String employeeId,
    String itemDescription,
    String qrCodePayload,
    Instant expiresAt
) {}
