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
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import se.enterprise.fikavault.api.dto.FikaRedeemRequest;
import se.enterprise.fikavault.api.dto.FikaVoucherResponse;
import se.enterprise.fikavault.domain.service.FikaService;
import se.enterprise.neatio.annotation.NeatioAudited;

/**
 * REST resource for Fika voucher redemptions, instrumented with MicroProfile Metrics and Neatio auditing.
 */
@Path("/fika")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FikaResource {

    @Inject
    private FikaService fikaService;

    @POST
    @Path("/redeem")
    @NeatioAudited(action = "FIKA_REDEEM")
    @Timed(name = "fikaRedemptionTime", description = "Time taken to redeem Fika vouchers")
    @Counted(name = "fikaRedemptionsCount", description = "Total number of Fika voucher redemptions")
    public Response redeemFika(@Valid FikaRedeemRequest req) {
        FikaVoucherResponse voucher = fikaService.redeem(req.employeeId());
        return Response.ok(voucher).build();
    }
}
