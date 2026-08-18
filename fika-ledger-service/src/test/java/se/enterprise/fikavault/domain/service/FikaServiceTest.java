package se.enterprise.fikavault.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.fikavault.api.dto.FikaVoucherResponse;
import se.enterprise.fikavault.domain.exception.EmployeeNotFoundException;
import se.enterprise.fikavault.domain.exception.InsufficientKudosBalanceException;
import se.enterprise.fikavault.domain.model.EmployeeAccount;
import se.enterprise.fikavault.domain.repository.InMemoryLedgerRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FikaServiceTest {

    private InMemoryLedgerRepository repository;
    private FikaService fikaService;

    @BeforeEach
    void setUp() throws Exception {
        repository = new InMemoryLedgerRepository();
        repository.initSeedData();

        fikaService = new FikaService();

        var repoField = FikaService.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(fikaService, repository);

        var costField = FikaService.class.getDeclaredField("voucherCost");
        costField.setAccessible(true);
        costField.setInt(fikaService, 10);

        var validityField = FikaService.class.getDeclaredField("validityHours");
        validityField.setAccessible(true);
        validityField.setInt(fikaService, 72);

        var itemField = FikaService.class.getDeclaredField("itemDescription");
        itemField.setAccessible(true);
        itemField.set(fikaService, "Artisan Coffee & Fresh Cinnamon Bun");
    }

    @Test
    void shouldRedeemFikaVoucherSuccessfully() {
        FikaVoucherResponse voucher = fikaService.redeem("SE-1001");

        assertThat(voucher).isNotNull();
        assertThat(voucher.voucherId()).isNotNull();
        assertThat(voucher.employeeId()).isEqualTo("SE-1001");
        assertThat(voucher.itemDescription()).isEqualTo("Artisan Coffee & Fresh Cinnamon Bun");
        assertThat(voucher.qrCodePayload()).startsWith("FIKA-QR-");
        assertThat(voucher.expiresAt()).isNotNull();

        EmployeeAccount account = repository.findAccount("SE-1001").orElseThrow();
        assertThat(account.getBalance()).isEqualTo(40); // 50 - 10
        assertThat(account.getVouchersRedeemed()).isEqualTo(1);
    }

    @Test
    void shouldThrowInsufficientBalanceExceptionWhenRedeemingWithLowBalance() {
        // SE-9821 has 4 credits, requires 10
        assertThatThrownBy(() -> fikaService.redeem("SE-9821"))
                .isInstanceOf(InsufficientKudosBalanceException.class)
                .hasMessage("Employee 'SE-9821' has 4 credits, but 10 are required for a Fika Voucher.");
    }

    @Test
    void shouldThrowWhenEmployeeNotFound() {
        assertThatThrownBy(() -> fikaService.redeem("SE-UNKNOWN"))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("SE-UNKNOWN");
    }
}
