package se.enterprise.fikavault;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.fikavault.boundary.dto.KudosCategory;
import se.enterprise.fikavault.boundary.dto.KudosTransferRequest;
import se.enterprise.fikavault.entity.InsufficientKudosBalanceException;
import se.enterprise.neatio.exception.ConstraintViolationExceptionMapper;
import se.enterprise.neatio.exception.NeatioExceptionMapper;
import se.enterprise.neatio.filter.CorrelationContext;
import se.enterprise.neatio.model.ProblemDetail;

import java.net.URI;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FikaLedgerPipelineTest {

    private static Validator validator;
    private CorrelationContext correlationContext;
    private UriInfo uriInfo;
    private ConstraintViolationExceptionMapper validationMapper;
    private NeatioExceptionMapper neatioExceptionMapper;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() throws Exception {
        correlationContext = new CorrelationContext();
        correlationContext.setCorrelationId("c83b4b8c-572f-4882-93cb-3392305a415b");

        uriInfo = mock(UriInfo.class);

        validationMapper = new ConstraintViolationExceptionMapper();
        var vCidField = ConstraintViolationExceptionMapper.class.getDeclaredField("correlationContext");
        vCidField.setAccessible(true);
        vCidField.set(validationMapper, correlationContext);
        var vUriField = ConstraintViolationExceptionMapper.class.getDeclaredField("uriInfo");
        vUriField.setAccessible(true);
        vUriField.set(validationMapper, uriInfo);

        neatioExceptionMapper = new NeatioExceptionMapper();
        var nCidField = NeatioExceptionMapper.class.getDeclaredField("correlationContext");
        nCidField.setAccessible(true);
        nCidField.set(neatioExceptionMapper, correlationContext);
        var nUriField = NeatioExceptionMapper.class.getDeclaredField("uriInfo");
        nUriField.setAccessible(true);
        nUriField.set(neatioExceptionMapper, uriInfo);
    }

    @Test
    void shouldProduceRfc7807OnValidationFailureMatchingPrd() {
        when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:9080/api/v1/kudos"));

        // Invalid request: amount is 0 (min is 1)
        KudosTransferRequest invalidReq = new KudosTransferRequest(
                "SE-1001",
                "SE-1002",
                0,
                "Thanks!",
                KudosCategory.TEAMWORK
        );

        Set<ConstraintViolation<KudosTransferRequest>> violations = validator.validate(invalidReq);
        assertThat(violations).isNotEmpty();

        jakarta.validation.ConstraintViolationException cve = new jakarta.validation.ConstraintViolationException(violations);
        Response response = validationMapper.toResponse(cve);

        assertThat(response.getStatus()).isEqualTo(400);
        ProblemDetail problem = (ProblemDetail) response.getEntity();
        assertThat(problem).isNotNull();
        assertThat(problem.getType()).isEqualTo(URI.create("https://neatio.internal/errors/validation-failed"));
        assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).isEqualTo("Input payload failed validation rules");
        assertThat(problem.getInstance()).isEqualTo(URI.create("/api/v1/kudos"));
        assertThat(problem.getCorrelationId()).isEqualTo("c83b4b8c-572f-4882-93cb-3392305a415b");
        assertThat(problem.getInvalidParameters()).anyMatch(param ->
                "amount".equals(param.field()) && Integer.valueOf(0).equals(param.rejectedValue())
        );
    }

    @Test
    void shouldProduceRfc7807OnInsufficientBalanceMatchingPrd() {
        when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:9080/api/v1/fika/redeem"));

        InsufficientKudosBalanceException ex = new InsufficientKudosBalanceException("SE-9821", 4, 10);
        Response response = neatioExceptionMapper.toResponse(ex);

        assertThat(response.getStatus()).isEqualTo(422);
        ProblemDetail problem = (ProblemDetail) response.getEntity();
        assertThat(problem).isNotNull();
        assertThat(problem.getType()).isEqualTo(URI.create("https://neatio.internal/errors/insufficient-balance"));
        assertThat(problem.getTitle()).isEqualTo("Insufficient Kudos Balance");
        assertThat(problem.getStatus()).isEqualTo(422);
        assertThat(problem.getDetail()).isEqualTo("Employee 'SE-9821' has 4 credits, but 10 are required for a Fika Voucher.");
        assertThat(problem.getInstance()).isEqualTo(URI.create("/api/v1/fika/redeem"));
        assertThat(problem.getCorrelationId()).isEqualTo("c83b4b8c-572f-4882-93cb-3392305a415b");
        assertThat(problem.getProperties()).containsEntry("employeeId", "SE-9821");
    }
}
