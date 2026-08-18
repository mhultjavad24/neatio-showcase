package se.enterprise.fikavault.domain.exception;

import se.enterprise.neatio.exception.BusinessRuleException;

import java.net.URI;

/**
 * Domain exception thrown when an employee has insufficient Kudos credits for a transaction or voucher redemption.
 */
public class InsufficientKudosBalanceException extends BusinessRuleException {

    private static final URI INSUFFICIENT_BALANCE_TYPE = URI.create("https://neatio.internal/errors/insufficient-balance");
    private static final String TITLE = "Insufficient Kudos Balance";

    public InsufficientKudosBalanceException(String employeeId, int currentBalance, int requiredCredits) {
        super(INSUFFICIENT_BALANCE_TYPE, TITLE,
                String.format("Employee '%s' has %d credits, but %d are required for a Fika Voucher.",
                        employeeId, currentBalance, requiredCredits));
        withExtension("employeeId", employeeId);
        withExtension("currentBalance", currentBalance);
        withExtension("requiredCredits", requiredCredits);
    }

    public InsufficientKudosBalanceException(String employeeId, int currentBalance, int requiredCredits, String purpose) {
        super(INSUFFICIENT_BALANCE_TYPE, TITLE,
                String.format("Employee '%s' has %d credits, but %d are required for %s.",
                        employeeId, currentBalance, requiredCredits, purpose));
        withExtension("employeeId", employeeId);
        withExtension("currentBalance", currentBalance);
        withExtension("requiredCredits", requiredCredits);
    }
}
