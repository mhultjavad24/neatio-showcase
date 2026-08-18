package se.enterprise.fikavault.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * MicroProfile Health Liveness check verifying the microservice runtime state.
 */
@Liveness
@ApplicationScoped
public class LedgerLivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("fika-ledger-liveness")
                .up()
                .withData("runtime", "Open Liberty")
                .withData("framework", "Neatio")
                .build();
    }
}
