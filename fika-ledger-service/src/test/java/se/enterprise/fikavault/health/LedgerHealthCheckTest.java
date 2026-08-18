package se.enterprise.fikavault.health;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;
import se.enterprise.fikavault.control.InMemoryLedgerRepository;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerHealthCheckTest {

    @Test
    void livenessCheckShouldReturnUp() {
        LedgerLivenessCheck check = new LedgerLivenessCheck();
        HealthCheckResponse response = check.call();

        assertThat(response.getName()).isEqualTo("fika-ledger-liveness");
        assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
    }

    @Test
    void readinessCheckShouldReturnUpWhenRepositoryHasAccounts() throws Exception {
        InMemoryLedgerRepository repo = new InMemoryLedgerRepository();
        repo.initSeedData();

        LedgerReadinessCheck check = new LedgerReadinessCheck();
        var repoField = LedgerReadinessCheck.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(check, repo);

        HealthCheckResponse response = check.call();

        assertThat(response.getName()).isEqualTo("fika-ledger-readiness");
        assertThat(response.getStatus()).isEqualTo(HealthCheckResponse.Status.UP);
        Object loaded = response.getData().orElseThrow().get("accountsLoaded");
        assertThat(((Number) loaded).intValue()).isEqualTo(5);
    }
}
