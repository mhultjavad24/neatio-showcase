package se.enterprise.fikavault.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.enterprise.fikavault.boundary.dto.LedgerSummaryResponse;
import se.enterprise.fikavault.boundary.dto.TransactionRecord;
import se.enterprise.fikavault.entity.EmployeeAccount;
import se.enterprise.fikavault.entity.EmployeeNotFoundException;
import se.enterprise.fikavault.control.InMemoryLedgerRepository;
import se.enterprise.neatio.annotation.NeatioLogged;

import java.util.List;

/**
 * Control providing ledger balance and transaction history queries.
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
