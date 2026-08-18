package se.enterprise.fikavault.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.fikavault.api.dto.LedgerSummaryResponse;
import se.enterprise.fikavault.domain.exception.EmployeeNotFoundException;
import se.enterprise.fikavault.domain.repository.InMemoryLedgerRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerServiceTest {

    private InMemoryLedgerRepository repository;
    private LedgerService ledgerService;

    @BeforeEach
    void setUp() throws Exception {
        repository = new InMemoryLedgerRepository();
        repository.initSeedData();

        ledgerService = new LedgerService();
        var repoField = LedgerService.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(ledgerService, repository);
    }

    @Test
    void shouldReturnLedgerSummaryForExistingEmployee() {
        LedgerSummaryResponse summary = ledgerService.getLedgerSummary("SE-9821");

        assertThat(summary).isNotNull();
        assertThat(summary.employeeId()).isEqualTo("SE-9821");
        assertThat(summary.employeeName()).isEqualTo("Linnea Lindqvist");
        assertThat(summary.balance()).isEqualTo(4);
        assertThat(summary.recentTransactions()).isNotEmpty();
    }

    @Test
    void shouldThrowWhenEmployeeNotFound() {
        assertThatThrownBy(() -> ledgerService.getLedgerSummary("NONEXISTENT"))
                .isInstanceOf(EmployeeNotFoundException.class);
    }
}
