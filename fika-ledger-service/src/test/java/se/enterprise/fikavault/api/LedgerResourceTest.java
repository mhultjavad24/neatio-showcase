package se.enterprise.fikavault.api;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.fikavault.api.dto.LedgerSummaryResponse;
import se.enterprise.fikavault.domain.service.LedgerService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LedgerResourceTest {

    private LedgerResource resource;
    private LedgerService ledgerService;

    @BeforeEach
    void setUp() throws Exception {
        resource = new LedgerResource();
        ledgerService = mock(LedgerService.class);

        var field = LedgerResource.class.getDeclaredField("ledgerService");
        field.setAccessible(true);
        field.set(resource, ledgerService);
    }

    @Test
    void shouldReturn200OkWithLedgerSummary() {
        LedgerSummaryResponse expected = new LedgerSummaryResponse(
                "SE-9821",
                "Linnea Lindqvist",
                4,
                0,
                4,
                0,
                Collections.emptyList()
        );

        when(ledgerService.getLedgerSummary("SE-9821")).thenReturn(expected);

        Response response = resource.getLedgerSummary("SE-9821");

        assertThat(response.getStatus()).isEqualTo(200);
        LedgerSummaryResponse entity = (LedgerSummaryResponse) response.getEntity();
        assertThat(entity).isNotNull();
        assertThat(entity.employeeId()).isEqualTo("SE-9821");
        assertThat(entity.balance()).isEqualTo(4);
    }
}
