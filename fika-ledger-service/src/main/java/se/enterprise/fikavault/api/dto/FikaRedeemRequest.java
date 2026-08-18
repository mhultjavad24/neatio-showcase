package se.enterprise.fikavault.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Inbound request model for redeeming Kudos for a Fika café voucher.
 */
public record FikaRedeemRequest(
    @NotBlank(message = "{fika.employee.required}")
    String employeeId
) {}
