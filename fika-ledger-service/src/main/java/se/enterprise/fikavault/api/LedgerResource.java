package se.enterprise.fikavault.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import se.enterprise.fikavault.api.dto.LedgerSummaryResponse;
import se.enterprise.fikavault.domain.service.LedgerService;
import se.enterprise.neatio.annotation.NeatioAudited;

/**
 * REST resource for querying employee Kudos balances and ledger transaction histories.
 */
@Path("/ledger")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class LedgerResource {

    @Inject
    private LedgerService ledgerService;

    @GET
    @Path("/{employeeId}")
    @NeatioAudited(action = "LEDGER_QUERY")
    public Response getLedgerSummary(@PathParam("employeeId") String employeeId) {
        LedgerSummaryResponse summary = ledgerService.getLedgerSummary(employeeId);
        return Response.ok(summary).build();
    }
}
