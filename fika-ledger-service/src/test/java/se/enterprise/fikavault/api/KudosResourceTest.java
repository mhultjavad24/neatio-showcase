package se.enterprise.fikavault.api;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.fikavault.api.dto.KudosCategory;
import se.enterprise.fikavault.api.dto.KudosTransferRequest;
import se.enterprise.fikavault.api.dto.KudosTransferResponse;
import se.enterprise.fikavault.domain.service.KudosService;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KudosResourceTest {

    private KudosResource resource;
    private KudosService kudosService;

    @BeforeEach
    void setUp() throws Exception {
        resource = new KudosResource();
        kudosService = mock(KudosService.class);

        var field = KudosResource.class.getDeclaredField("kudosService");
        field.setAccessible(true);
        field.set(resource, kudosService);
    }

    @Test
    void shouldReturn201CreatedOnSuccessfulTransfer() {
        KudosTransferRequest request = new KudosTransferRequest(
                "SE-1001",
                "SE-1002",
                10,
                "Great pair programming session!",
                KudosCategory.TEAMWORK
        );

        UUID txId = UUID.randomUUID();
        when(kudosService.transfer(any(KudosTransferRequest.class)))
                .thenReturn(new KudosTransferResponse(txId, "COMPLETED", Instant.now()));

        Response response = resource.sendKudos(request);

        assertThat(response.getStatus()).isEqualTo(201);
        KudosTransferResponse entity = (KudosTransferResponse) response.getEntity();
        assertThat(entity).isNotNull();
        assertThat(entity.transactionId()).isEqualTo(txId);
        assertThat(entity.status()).isEqualTo("COMPLETED");
    }
}
