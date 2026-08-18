package se.enterprise.fikavault.it;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.enterprise.fikavault.domain.repository.InMemoryLedgerRepository;
import se.enterprise.fikavault.health.LedgerLivenessCheck;
import se.enterprise.fikavault.health.LedgerReadinessCheck;

/**
 * TestNG Integration test suite validating MicroProfile Health checks.
 */
public class FikaLedgerHealthAndMetricsIT {

    private LedgerLivenessCheck livenessCheck;
    private LedgerReadinessCheck readinessCheck;
    private InMemoryLedgerRepository repository;

    @BeforeClass
    public void setup() throws Exception {
        livenessCheck = new LedgerLivenessCheck();
        readinessCheck = new LedgerReadinessCheck();

        repository = new InMemoryLedgerRepository();
        repository.initSeedData();

        var repoField = LedgerReadinessCheck.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(readinessCheck, repository);
    }

    @Test(description = "Verifies MP Health Liveness probe returns UP")
    public void testLivenessProbe() {
        HealthCheckResponse res = livenessCheck.call();

        Assert.assertEquals(res.getName(), "fika-ledger-liveness");
        Assert.assertEquals(res.getStatus(), HealthCheckResponse.Status.UP);
        Assert.assertEquals(res.getData().orElseThrow().get("runtime"), "Open Liberty");
    }

    @Test(description = "Verifies MP Health Readiness probe returns UP with active accounts")
    public void testReadinessProbe() {
        HealthCheckResponse res = readinessCheck.call();

        Assert.assertEquals(res.getName(), "fika-ledger-readiness");
        Assert.assertEquals(res.getStatus(), HealthCheckResponse.Status.UP);
        Object loaded = res.getData().orElseThrow().get("accountsLoaded");
        Assert.assertEquals(((Number) loaded).intValue(), 5);
    }
}
