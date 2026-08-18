package se.enterprise.fikavault.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import se.enterprise.fikavault.domain.repository.InMemoryLedgerRepository;

/**
 * MicroProfile Health Readiness check verifying that domain repositories are initialized.
 */
@Readiness
@ApplicationScoped
public class LedgerReadinessCheck implements HealthCheck {

    @Inject
    private InMemoryLedgerRepository repository;

    @Override
    public HealthCheckResponse call() {
        int count = repository != null ? repository.totalAccounts() : 0;
        boolean ready = count > 0;

        return HealthCheckResponse.named("fika-ledger-readiness")
                .status(ready)
                .withData("accountsLoaded", count)
                .build();
    }
}
