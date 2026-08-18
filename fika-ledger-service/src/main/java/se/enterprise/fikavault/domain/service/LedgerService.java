package se.enterprise.fikavault.domain.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.enterprise.fikavault.api.dto.LedgerSummaryResponse;
import se.enterprise.fikavault.api.dto.TransactionRecord;
import se.enterprise.fikavault.domain.exception.EmployeeNotFoundException;
import se.enterprise.fikavault.domain.model.EmployeeAccount;
import se.enterprise.fikavault.domain.repository.InMemoryLedgerRepository;
import se.enterprise.neatio.annotation.NeatioLogged;

import java.util.List;

/**
 * Service providing ledger balance and transaction history queries.
 */
@ApplicationScoped
public class LedgerService {

    @Inject
    private InMemoryLedgerRepository repository;

    @NeatioLogged
    public LedgerSummaryResponse getLedgerSummary(String employeeId) {
        EmployeeAccount account = repository.findAccount(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        List<TransactionRecord> transactions = repository.getTransactionsForEmployee(employeeId);

        return new LedgerSummaryResponse(
                account.getEmployeeId(),
                account.getFullName(),
                account.getBalance(),
                account.getTotalSent(),
                account.getTotalReceived(),
                account.getVouchersRedeemed(),
                transactions
        );
    }
}
