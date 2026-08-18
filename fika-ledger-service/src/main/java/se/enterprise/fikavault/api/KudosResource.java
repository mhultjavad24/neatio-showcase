package se.enterprise.fikavault.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import se.enterprise.fikavault.api.dto.KudosTransferRequest;
import se.enterprise.fikavault.api.dto.KudosTransferResponse;
import se.enterprise.fikavault.domain.service.KudosService;
import se.enterprise.neatio.annotation.NeatioAudited;

/**
 * REST resource for peer-to-peer Kudos transfers.
 */
@Path("/kudos")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KudosResource {

    @Inject
    private KudosService kudosService;

    @POST
    @NeatioAudited(action = "KUDOS_TRANSFER")
    public Response sendKudos(@Valid KudosTransferRequest req) {
        KudosTransferResponse receipt = kudosService.transfer(req);
        return Response.status(Response.Status.CREATED).entity(receipt).build();
    }
}
