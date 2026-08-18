package se.enterprise.fikavault.it;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.enterprise.fikavault.boundary.FikaResource;
import se.enterprise.fikavault.boundary.KudosResource;
import se.enterprise.fikavault.boundary.LedgerResource;
import se.enterprise.fikavault.boundary.dto.FikaRedeemRequest;
import se.enterprise.fikavault.boundary.dto.FikaVoucherResponse;
import se.enterprise.fikavault.boundary.dto.KudosCategory;
import se.enterprise.fikavault.boundary.dto.KudosTransferRequest;
import se.enterprise.fikavault.boundary.dto.KudosTransferResponse;
import se.enterprise.fikavault.boundary.dto.LedgerSummaryResponse;
import se.enterprise.fikavault.control.FikaService;
import se.enterprise.fikavault.control.InMemoryLedgerRepository;
import se.enterprise.fikavault.control.KudosService;
import se.enterprise.fikavault.control.LedgerService;
import se.enterprise.fikavault.entity.EmployeeAccount;
import se.enterprise.fikavault.entity.InsufficientKudosBalanceException;
import se.enterprise.neatio.exception.BusinessRuleException;
import se.enterprise.neatio.exception.ConstraintViolationExceptionMapper;
import se.enterprise.neatio.exception.NeatioExceptionMapper;
import se.enterprise.neatio.filter.CorrelationContext;
import se.enterprise.neatio.model.ProblemDetail;

import java.net.URI;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestNG Integration test suite validating end-to-end business workflows,
 * transactions, voucher redemptions, and RFC 7807 error envelopes.
 */
public class FikaLedgerIntegrationIT {

    private InMemoryLedgerRepository repository;
    private KudosService kudosService;
    private FikaService fikaService;
    private LedgerService ledgerService;

    private KudosResource kudosResource;
    private FikaResource fikaResource;
    private LedgerResource ledgerResource;

    private CorrelationContext correlationContext;
    private NeatioExceptionMapper exceptionMapper;
    private ConstraintViolationExceptionMapper validationMapper;
    private Validator validator;

    @BeforeClass
    public void setup() throws Exception {
        correlationContext = new CorrelationContext();
        correlationContext.setCorrelationId("it-session-correlation-id-777");

        repository = new InMemoryLedgerRepository();
        repository.initSeedData();

        kudosService = new KudosService();
        var kRepoField = KudosService.class.getDeclaredField("repository");
        kRepoField.setAccessible(true);
        kRepoField.set(kudosService, repository);

        fikaService = new FikaService();
        var fRepoField = FikaService.class.getDeclaredField("repository");
        fRepoField.setAccessible(true);
        fRepoField.set(fikaService, repository);
        var costField = FikaService.class.getDeclaredField("voucherCost");
        costField.setAccessible(true);
        costField.setInt(fikaService, 10);
        var valField = FikaService.class.getDeclaredField("validityHours");
        valField.setAccessible(true);
        valField.setInt(fikaService, 72);
        var itemField = FikaService.class.getDeclaredField("itemDescription");
        itemField.setAccessible(true);
        itemField.set(fikaService, "Artisan Coffee & Fresh Cinnamon Bun");

        ledgerService = new LedgerService();
        var lRepoField = LedgerService.class.getDeclaredField("repository");
        lRepoField.setAccessible(true);
        lRepoField.set(ledgerService, repository);

        kudosResource = new KudosResource();
        var krServiceField = KudosResource.class.getDeclaredField("kudosService");
        krServiceField.setAccessible(true);
        krServiceField.set(kudosResource, kudosService);

        fikaResource = new FikaResource();
        var frServiceField = FikaResource.class.getDeclaredField("fikaService");
        frServiceField.setAccessible(true);
        frServiceField.set(fikaResource, fikaService);

        ledgerResource = new LedgerResource();
        var lrServiceField = LedgerResource.class.getDeclaredField("ledgerService");
        lrServiceField.setAccessible(true);
        lrServiceField.set(ledgerResource, ledgerService);

        exceptionMapper = new NeatioExceptionMapper();
        var exCid = NeatioExceptionMapper.class.getDeclaredField("correlationContext");
        exCid.setAccessible(true);
        exCid.set(exceptionMapper, correlationContext);

        validationMapper = new ConstraintViolationExceptionMapper();
        var valCid = ConstraintViolationExceptionMapper.class.getDeclaredField("correlationContext");
        valCid.setAccessible(true);
        valCid.set(validationMapper, correlationContext);

        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test(priority = 1, description = "1. Verify initial employee balances and profiles")
    public void testInitialState() {
        EmployeeAccount erik = repository.findAccount("SE-1001").orElseThrow();
        EmployeeAccount linnea = repository.findAccount("SE-9821").orElseThrow();

        Assert.assertEquals(erik.getBalance(), 50);
        Assert.assertEquals(linnea.getBalance(), 4);
    }

    @Test(priority = 2, description = "2. Transfer 20 Kudos from Erik (SE-1001) to Linnea (SE-9821)")
    public void testKudosTransferSuccess() {
        KudosTransferRequest request = new KudosTransferRequest(
                "SE-1001",
                "SE-9821",
                20,
                "Amazing work leading the Neatio release!",
                KudosCategory.LEADERSHIP
        );

        Response response = kudosResource.sendKudos(request);
        Assert.assertEquals(response.getStatus(), 201);

        KudosTransferResponse receipt = (KudosTransferResponse) response.getEntity();
        Assert.assertNotNull(receipt);
        Assert.assertEquals(receipt.status(), "COMPLETED");

        EmployeeAccount erik = repository.findAccount("SE-1001").orElseThrow();
        EmployeeAccount linnea = repository.findAccount("SE-9821").orElseThrow();

        Assert.assertEquals(erik.getBalance(), 30); // 50 - 20
        Assert.assertEquals(linnea.getBalance(), 24); // 4 + 20
    }

    @Test(priority = 3, description = "3. Linnea redeems first Fika Voucher (cost 10 credits)")
    public void testFirstFikaVoucherRedemption() {
        FikaRedeemRequest req = new FikaRedeemRequest("SE-9821");

        Response response = fikaResource.redeemFika(req);
        Assert.assertEquals(response.getStatus(), 200);

        FikaVoucherResponse voucher = (FikaVoucherResponse) response.getEntity();
        Assert.assertNotNull(voucher);
        Assert.assertEquals(voucher.employeeId(), "SE-9821");
        Assert.assertTrue(voucher.qrCodePayload().startsWith("FIKA-QR-"));

        EmployeeAccount linnea = repository.findAccount("SE-9821").orElseThrow();
        Assert.assertEquals(linnea.getBalance(), 14); // 24 - 10
        Assert.assertEquals(linnea.getVouchersRedeemed(), 1);
    }

    @Test(priority = 4, description = "4. Linnea redeems second Fika Voucher (cost 10 credits)")
    public void testSecondFikaVoucherRedemption() {
        FikaRedeemRequest req = new FikaRedeemRequest("SE-9821");

        Response response = fikaResource.redeemFika(req);
        Assert.assertEquals(response.getStatus(), 200);

        EmployeeAccount linnea = repository.findAccount("SE-9821").orElseThrow();
        Assert.assertEquals(linnea.getBalance(), 4); // 14 - 10
        Assert.assertEquals(linnea.getVouchersRedeemed(), 2);
    }

    @Test(priority = 5, description = "5. Linnea attempts 3rd redemption with 4 credits -> HTTP 422 RFC 7807")
    public void testThirdFikaVoucherRedemptionInsufficientBalance() throws Exception {
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:9080/api/v1/fika/redeem"));

        var uriField = NeatioExceptionMapper.class.getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(exceptionMapper, uriInfo);

        try {
            fikaResource.redeemFika(new FikaRedeemRequest("SE-9821"));
            Assert.fail("Expected InsufficientKudosBalanceException");
        } catch (InsufficientKudosBalanceException ex) {
            Response errorResponse = exceptionMapper.toResponse(ex);
            Assert.assertEquals(errorResponse.getStatus(), 422);

            ProblemDetail problem = (ProblemDetail) errorResponse.getEntity();
            Assert.assertNotNull(problem);
            Assert.assertEquals(problem.getType(), URI.create("https://neatio.internal/errors/insufficient-balance"));
            Assert.assertEquals(problem.getTitle(), "Insufficient Kudos Balance");
            Assert.assertEquals(problem.getStatus(), 422);
            Assert.assertEquals(problem.getDetail(), "Employee 'SE-9821' has 4 credits, but 10 are required for a Fika Voucher.");
            Assert.assertEquals(problem.getInstance(), URI.create("/api/v1/fika/redeem"));
            Assert.assertEquals(problem.getCorrelationId(), "it-session-correlation-id-777");
        }
    }

    @Test(priority = 6, description = "6. Invalid Kudos transfer payload -> HTTP 400 RFC 7807")
    public void testValidationFailureOnInvalidKudosTransfer() throws Exception {
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:9080/api/v1/kudos"));

        var uriField = ConstraintViolationExceptionMapper.class.getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(validationMapper, uriInfo);

        KudosTransferRequest invalidReq = new KudosTransferRequest(
                "", // Blank sender
                "SE-1002",
                0,  // Min is 1
                "Hi", // Min 5 chars
                KudosCategory.TEAMWORK
        );

        Set<?> violations = validator.validate(invalidReq);
        Assert.assertFalse(violations.isEmpty());

        jakarta.validation.ConstraintViolationException cve = new jakarta.validation.ConstraintViolationException((Set) violations);
        Response response = validationMapper.toResponse(cve);

        Assert.assertEquals(response.getStatus(), 400);
        ProblemDetail problem = (ProblemDetail) response.getEntity();
        Assert.assertNotNull(problem);
        Assert.assertEquals(problem.getStatus(), 400);
        Assert.assertEquals(problem.getType(), URI.create("https://neatio.internal/errors/validation-failed"));
        Assert.assertEquals(problem.getTitle(), "Constraint Violation");
        Assert.assertEquals(problem.getCorrelationId(), "it-session-correlation-id-777");
        Assert.assertTrue(problem.getInvalidParameters().size() >= 3);
    }

    @Test(priority = 7, description = "7. Self transfer attempt -> HTTP 422 RFC 7807")
    public void testSelfTransferRejection() throws Exception {
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:9080/api/v1/kudos"));

        var uriField = NeatioExceptionMapper.class.getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(exceptionMapper, uriInfo);

        try {
            kudosResource.sendKudos(new KudosTransferRequest("SE-1001", "SE-1001", 5, "Self appreciation", KudosCategory.KINDNESS));
            Assert.fail("Expected BusinessRuleException for self-transfer");
        } catch (BusinessRuleException ex) {
            Response errorResponse = exceptionMapper.toResponse(ex);
            Assert.assertEquals(errorResponse.getStatus(), 422);

            ProblemDetail problem = (ProblemDetail) errorResponse.getEntity();
            Assert.assertEquals(problem.getStatus(), 422);
            Assert.assertEquals(problem.getDetail(), "Employees cannot transfer Kudos to themselves.");
        }
    }

    @Test(priority = 8, description = "8. Query Ledger summaries and audit records")
    public void testLedgerSummaryVerification() {
        Response linneaSummaryRes = ledgerResource.getLedgerSummary("SE-9821");
        Assert.assertEquals(linneaSummaryRes.getStatus(), 200);

        LedgerSummaryResponse linneaSummary = (LedgerSummaryResponse) linneaSummaryRes.getEntity();
        Assert.assertNotNull(linneaSummary);
        Assert.assertEquals(linneaSummary.employeeId(), "SE-9821");
        Assert.assertEquals(linneaSummary.balance(), 4);
        Assert.assertEquals(linneaSummary.fikaVouchersRedeemed(), 2);
        Assert.assertTrue(linneaSummary.recentTransactions().size() >= 2);

        Response erikSummaryRes = ledgerResource.getLedgerSummary("SE-1001");
        Assert.assertEquals(erikSummaryRes.getStatus(), 200);

        LedgerSummaryResponse erikSummary = (LedgerSummaryResponse) erikSummaryRes.getEntity();
        Assert.assertEquals(erikSummary.balance(), 30);
        Assert.assertEquals(erikSummary.totalSent(), 20);
    }
}
