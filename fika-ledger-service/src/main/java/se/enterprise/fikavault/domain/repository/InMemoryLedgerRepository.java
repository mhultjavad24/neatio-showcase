package se.enterprise.fikavault.domain.repository;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import se.enterprise.fikavault.api.dto.TransactionRecord;
import se.enterprise.fikavault.domain.model.EmployeeAccount;
import se.enterprise.fikavault.domain.model.FikaVoucher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory repository providing thread-safe storage for employee accounts,
 * transaction ledgers, and generated Fika vouchers.
 */
@ApplicationScoped
public class InMemoryLedgerRepository {

    private final Map<String, EmployeeAccount> accounts = new ConcurrentHashMap<>();
    private final List<TransactionRecord> transactions = new CopyOnWriteArrayList<>();
    private final List<FikaVoucher> vouchers = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void initSeedData() {
        // Seed initial employees
        // SE-9821 has 4 credits (matches PRD section 5.2 scenario)
        accounts.put("SE-9821", new EmployeeAccount("SE-9821", "Linnea Lindqvist", 4));
        accounts.put("SE-1001", new EmployeeAccount("SE-1001", "Erik Johansson", 50));
        accounts.put("SE-1002", new EmployeeAccount("SE-1002", "Astrid Berg", 25));
        accounts.put("SE-1003", new EmployeeAccount("SE-1003", "Sven Nilsson", 15));
        accounts.put("SE-1004", new EmployeeAccount("SE-1004", "Freja Larsson", 30));

        // Initial transaction history
        transactions.add(new TransactionRecord(
                UUID.fromString("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"),
                "KUDOS_TRANSFER",
                "SE-1001",
                "SE-9821",
                4,
                "Welcome to the team!",
                Instant.now().minusSeconds(86400)
        ));
    }

    public Optional<EmployeeAccount> findAccount(String employeeId) {
        return Optional.ofNullable(accounts.get(employeeId));
    }

    public void saveAccount(EmployeeAccount account) {
        accounts.put(account.getEmployeeId(), account);
    }

    public void recordTransaction(TransactionRecord transaction) {
        transactions.add(0, transaction); // prepend newest first
    }

    public List<TransactionRecord> getTransactionsForEmployee(String employeeId) {
        List<TransactionRecord> result = new ArrayList<>();
        for (TransactionRecord tx : transactions) {
            if (employeeId.equals(tx.senderId()) || employeeId.equals(tx.recipientId())) {
                result.add(tx);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public void saveVoucher(FikaVoucher voucher) {
        vouchers.add(0, voucher);
    }

    public List<FikaVoucher> getVouchersForEmployee(String employeeId) {
        List<FikaVoucher> result = new ArrayList<>();
        for (FikaVoucher v : vouchers) {
            if (employeeId.equals(v.employeeId())) {
                result.add(v);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public int totalAccounts() {
        return accounts.size();
    }
}
