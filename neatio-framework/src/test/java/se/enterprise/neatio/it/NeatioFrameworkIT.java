package se.enterprise.neatio.it;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.MDC;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import se.enterprise.neatio.exception.BusinessRuleException;
import se.enterprise.neatio.exception.ConstraintViolationExceptionMapper;
import se.enterprise.neatio.exception.NeatioExceptionMapper;
import se.enterprise.neatio.exception.ResourceNotFoundException;
import se.enterprise.neatio.filter.CorrelationContext;
import se.enterprise.neatio.filter.NeatioCorrelationFilter;
import se.enterprise.neatio.model.ProblemDetail;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestNG Integration test suite verifying the complete Neatio Framework pipeline.
 */
public class NeatioFrameworkIT {

    private NeatioCorrelationFilter filter;
    private CorrelationContext correlationContext;
    private NeatioExceptionMapper exceptionMapper;
    private ConstraintViolationExceptionMapper validationMapper;
    private Validator validator;

    record SamplePayload(
            @NotBlank(message = "Username cannot be blank")
            String username,

            @Min(value = 18, message = "Age must be at least 18")
            int age
    ) {}

    @BeforeClass
    public void setup() throws Exception {
        filter = new NeatioCorrelationFilter();
        correlationContext = new CorrelationContext();

        var filterCid = NeatioCorrelationFilter.class.getDeclaredField("correlationContext");
        filterCid.setAccessible(true);
        filterCid.set(filter, correlationContext);

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

    @Test(description = "Verifies correlation ID filter lifecycle from request to response")
    public void testCorrelationFilterLifecycle() throws IOException {
        String testCid = "it-cid-test-101";

        ContainerRequestContext reqCtx = mock(ContainerRequestContext.class);
        when(reqCtx.getHeaderString(NeatioCorrelationFilter.HEADER_NAME)).thenReturn(testCid);
        when(reqCtx.getProperty(NeatioCorrelationFilter.PROPERTY_KEY)).thenReturn(testCid);

        filter.filter(reqCtx);

        Assert.assertEquals(correlationContext.getCorrelationId(), testCid);
        Assert.assertEquals(MDC.get(NeatioCorrelationFilter.MDC_KEY), testCid);

        ContainerResponseContext resCtx = mock(ContainerResponseContext.class);
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(resCtx.getHeaders()).thenReturn(headers);

        filter.filter(reqCtx, resCtx);

        Assert.assertEquals(headers.getFirst(NeatioCorrelationFilter.HEADER_NAME), testCid);
        Assert.assertNull(MDC.get(NeatioCorrelationFilter.MDC_KEY));
    }

    @DataProvider(name = "exceptionScenarios")
    public Object[][] provideExceptionScenarios() {
        return new Object[][]{
                {new BusinessRuleException("Balance too low"), 422, "Business Rule Violation"},
                {new ResourceNotFoundException("Entity not found"), 404, "Resource Not Found"}
        };
    }

    @Test(dataProvider = "exceptionScenarios", description = "Verifies exception mapping to RFC 7807")
    public void testExceptionMappingToRfc7807(RuntimeException ex, int expectedStatus, String expectedTitle) throws Exception {
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:9080/api/v1/resource"));

        var uriField = NeatioExceptionMapper.class.getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(exceptionMapper, uriInfo);

        correlationContext.setCorrelationId("it-correlation-exception-test");

        Response response = exceptionMapper.toResponse(ex);

        Assert.assertEquals(response.getStatus(), expectedStatus);
        ProblemDetail problem = (ProblemDetail) response.getEntity();
        Assert.assertNotNull(problem);
        Assert.assertEquals(problem.getStatus(), expectedStatus);
        Assert.assertEquals(problem.getTitle(), expectedTitle);
        Assert.assertEquals(problem.getCorrelationId(), "it-correlation-exception-test");
        Assert.assertEquals(problem.getInstance(), URI.create("/api/v1/resource"));
    }

    @Test(description = "Verifies bean validation mapping into RFC 7807 with invalid parameter details")
    public void testBeanValidationMapping() throws Exception {
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:9080/api/v1/validate"));

        var uriField = ConstraintViolationExceptionMapper.class.getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(validationMapper, uriInfo);

        correlationContext.setCorrelationId("it-correlation-val-test");

        SamplePayload invalidPayload = new SamplePayload("", 15);
        Set<?> violations = validator.validate(invalidPayload);
        Assert.assertFalse(violations.isEmpty());

        jakarta.validation.ConstraintViolationException cve = new jakarta.validation.ConstraintViolationException((Set) violations);
        Response response = validationMapper.toResponse(cve);

        Assert.assertEquals(response.getStatus(), 400);
        ProblemDetail problem = (ProblemDetail) response.getEntity();
        Assert.assertNotNull(problem);
        Assert.assertEquals(problem.getStatus(), 400);
        Assert.assertEquals(problem.getTitle(), "Constraint Violation");
        Assert.assertEquals(problem.getCorrelationId(), "it-correlation-val-test");
        Assert.assertTrue(problem.getInvalidParameters().size() >= 2);
    }
}
