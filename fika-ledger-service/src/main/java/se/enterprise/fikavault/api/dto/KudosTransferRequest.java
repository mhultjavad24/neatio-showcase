package se.enterprise.fikavault.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound request model for peer-to-peer Kudos transfer.
 */
public record KudosTransferRequest(
    @NotBlank(message = "{kudos.sender.required}")
    String senderId,

    @NotBlank(message = "{kudos.recipient.required}")
    String recipientId,

    @Min(value = 1, message = "{kudos.amount.min}")
    @Max(value = 50, message = "{kudos.amount.max}")
    int amount,

    @Size(min = 5, max = 200, message = "{kudos.message.size}")
    String message,

    KudosCategory category
) {}
