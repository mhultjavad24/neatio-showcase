package se.enterprise.fikavault.boundary;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.fikavault.boundary.dto.FikaRedeemRequest;
import se.enterprise.fikavault.boundary.dto.FikaVoucherResponse;
import se.enterprise.fikavault.control.FikaService;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FikaResourceTest {

    private FikaResource resource;
    private FikaService fikaService;

    @BeforeEach
    void setUp() throws Exception {
        resource = new FikaResource();
        fikaService = mock(FikaService.class);

        var field = FikaResource.class.getDeclaredField("fikaService");
        field.setAccessible(true);
        field.set(resource, fikaService);
    }

    @Test
    void shouldReturn200OkOnVoucherRedemption() {
        FikaRedeemRequest req = new FikaRedeemRequest("SE-1001");
        UUID voucherId = UUID.randomUUID();
        Instant expiry = Instant.now().plusSeconds(3600);

        FikaVoucherResponse expected = new FikaVoucherResponse(
                voucherId,
                "SE-1001",
                "Artisan Coffee & Fresh Cinnamon Bun",
                "FIKA-QR-" + voucherId + "-SE-1001",
                expiry
        );

        when(fikaService.redeem("SE-1001")).thenReturn(expected);

        Response response = resource.redeemFika(req);

        assertThat(response.getStatus()).isEqualTo(200);
        FikaVoucherResponse entity = (FikaVoucherResponse) response.getEntity();
        assertThat(entity).isNotNull();
        assertThat(entity.voucherId()).isEqualTo(voucherId);
        assertThat(entity.employeeId()).isEqualTo("SE-1001");
        assertThat(entity.itemDescription()).isEqualTo("Artisan Coffee & Fresh Cinnamon Bun");
    }
}
