package se.enterprise.fikavault.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.fikavault.boundary.dto.TransactionRecord;
import se.enterprise.fikavault.entity.EmployeeAccount;
import se.enterprise.fikavault.entity.FikaVoucher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryLedgerRepositoryTest {

    private InMemoryLedgerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLedgerRepository();
        repository.initSeedData();
    }

    @Test
    void shouldFindPreSeededAccounts() {
        Optional<EmployeeAccount> se9821 = repository.findAccount("SE-9821");
        assertThat(se9821).isPresent();
        assertThat(se9821.get().getBalance()).isEqualTo(4);
        assertThat(se9821.get().getFullName()).isEqualTo("Linnea Lindqvist");

        assertThat(repository.findAccount("SE-NONEXISTENT")).isEmpty();
        assertThat(repository.totalAccounts()).isEqualTo(5);
    }

    @Test
    void shouldSaveAndRetrieveNewAccount() {
        EmployeeAccount newAccount = new EmployeeAccount("SE-9999", "New Person", 100);
        repository.saveAccount(newAccount);

        Optional<EmployeeAccount> retrieved = repository.findAccount("SE-9999");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getBalance()).isEqualTo(100);
        assertThat(repository.totalAccounts()).isEqualTo(6);
    }

    @Test
    void shouldRecordAndRetrieveTransactionsForEmployee() {
        UUID txId = UUID.randomUUID();
        TransactionRecord record = new TransactionRecord(
                txId,
                "KUDOS_TRANSFER",
                "SE-1001",
                "SE-1002",
                10,
                "Thanks!",
                Instant.now()
        );

        repository.recordTransaction(record);

        List<TransactionRecord> forSender = repository.getTransactionsForEmployee("SE-1001");
        assertThat(forSender).anyMatch(tx -> tx.id().equals(txId));

        List<TransactionRecord> forRecipient = repository.getTransactionsForEmployee("SE-1002");
        assertThat(forRecipient).anyMatch(tx -> tx.id().equals(txId));

        List<TransactionRecord> forUnrelated = repository.getTransactionsForEmployee("SE-9999");
        assertThat(forUnrelated).noneMatch(tx -> tx.id().equals(txId));
    }

    @Test
    void shouldSaveAndRetrieveVouchers() {
        UUID voucherId = UUID.randomUUID();
        FikaVoucher voucher = new FikaVoucher(
                voucherId,
                "SE-1001",
                "Coffee & Bun",
                "QR-12345",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                false
        );

        repository.saveVoucher(voucher);

        List<FikaVoucher> vouchers = repository.getVouchersForEmployee("SE-1001");
        assertThat(vouchers).hasSize(1);
        assertThat(vouchers.get(0).id()).isEqualTo(voucherId);
        assertThat(vouchers.get(0).itemDescription()).isEqualTo("Coffee & Bun");

        assertThat(repository.getVouchersForEmployee("SE-9821")).isEmpty();
    }
}
