package se.enterprise.fikavault.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.fikavault.api.dto.KudosCategory;
import se.enterprise.fikavault.api.dto.KudosTransferRequest;
import se.enterprise.fikavault.api.dto.KudosTransferResponse;
import se.enterprise.fikavault.domain.exception.EmployeeNotFoundException;
import se.enterprise.fikavault.domain.exception.InsufficientKudosBalanceException;
import se.enterprise.fikavault.domain.model.EmployeeAccount;
import se.enterprise.fikavault.domain.repository.InMemoryLedgerRepository;
import se.enterprise.neatio.exception.BusinessRuleException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KudosServiceTest {

    private InMemoryLedgerRepository repository;
    private KudosService kudosService;

    @BeforeEach
    void setUp() throws Exception {
        repository = new InMemoryLedgerRepository();
        repository.initSeedData();

        kudosService = new KudosService();
        var repoField = KudosService.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(kudosService, repository);
    }

    @Test
    void shouldTransferKudosSuccessfully() {
        KudosTransferRequest request = new KudosTransferRequest(
                "SE-1001",
                "SE-1002",
                15,
                "Great work on the Neatio architecture!",
                KudosCategory.CRAFTSMANSHIP
        );

        KudosTransferResponse response = kudosService.transfer(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.transactionId()).isNotNull();

        EmployeeAccount sender = repository.findAccount("SE-1001").orElseThrow();
        EmployeeAccount recipient = repository.findAccount("SE-1002").orElseThrow();

        assertThat(sender.getBalance()).isEqualTo(35); // 50 - 15
        assertThat(sender.getTotalSent()).isEqualTo(15);
        assertThat(recipient.getBalance()).isEqualTo(40); // 25 + 15
        assertThat(recipient.getTotalReceived()).isEqualTo(15);
    }

    @Test
    void shouldThrowInsufficientBalanceExceptionWhenSenderHasNotEnoughCredits() {
        KudosTransferRequest request = new KudosTransferRequest(
                "SE-9821", // has 4 credits
                "SE-1001",
                10,
                "Thanks for the help",
                KudosCategory.TEAMWORK
        );

        assertThatThrownBy(() -> kudosService.transfer(request))
                .isInstanceOf(InsufficientKudosBalanceException.class)
                .hasMessageContaining("Employee 'SE-9821' has 4 credits, but 10 are required for Kudos transfer.");
    }

    @Test
    void shouldThrowWhenTransferringToSelf() {
        KudosTransferRequest request = new KudosTransferRequest(
                "SE-1001",
                "SE-1001",
                5,
                "Self kudos",
                KudosCategory.TEAMWORK
        );

        assertThatThrownBy(() -> kudosService.transfer(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Employees cannot transfer Kudos to themselves.");
    }

    @Test
    void shouldThrowWhenSenderNotFound() {
        KudosTransferRequest request = new KudosTransferRequest(
                "SE-NONEXISTENT",
                "SE-1001",
                5,
                "Hello",
                KudosCategory.TEAMWORK
        );

        assertThatThrownBy(() -> kudosService.transfer(request))
                .isInstanceOf(EmployeeNotFoundException.class);
    }
}
