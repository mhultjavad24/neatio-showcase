package se.enterprise.fikavault.api.dto;

import java.util.List;

/**
 * Summary view of an employee's Kudos balance and recent transactions.
 */
public record LedgerSummaryResponse(
    String employeeId,
    String employeeName,
    int balance,
    int totalSent,
    int totalReceived,
    int fikaVouchersRedeemed,
    List<TransactionRecord> recentTransactions
) {}
