package se.enterprise.neatio.exception;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.enterprise.neatio.filter.CorrelationContext;
import se.enterprise.neatio.model.InvalidParameter;
import se.enterprise.neatio.model.ProblemDetail;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * JAX-RS ExceptionMapper converting Jakarta Bean Validation ConstraintViolationExceptions
 * into unified RFC 7807 Problem Details responses.
 */
@Provider
@Priority(Priorities.USER - 10)
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger log = LoggerFactory.getLogger(ConstraintViolationExceptionMapper.class);

    private static final URI VALIDATION_ERROR_TYPE = URI.create("https://neatio.internal/errors/validation-failed");
    private static final String VALIDATION_ERROR_TITLE = "Constraint Violation";
    private static final String DEFAULT_DETAIL = "Input payload failed validation rules";

    @Context
    private UriInfo uriInfo;

    @Inject
    private CorrelationContext correlationContext;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String correlationId = correlationContext != null ? correlationContext.getCorrelationId() : "N/A";
        String requestPath = uriInfo != null ? uriInfo.getRequestUri().getPath() : "/unknown";

        log.debug("[VALIDATION_FAILED] correlationId={} path={} violations={}",
                correlationId, requestPath, exception.getConstraintViolations().size());

        List<InvalidParameter> invalidParams = new ArrayList<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String fieldName = extractFieldName(violation.getPropertyPath());
            Object invalidValue = violation.getInvalidValue();
            String message = violation.getMessage();
            invalidParams.add(new InvalidParameter(fieldName, invalidValue, message));
        }

        ProblemDetail problem = ProblemDetail.builder()
                .type(VALIDATION_ERROR_TYPE)
                .title(VALIDATION_ERROR_TITLE)
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .detail(DEFAULT_DETAIL)
                .instance(requestPath)
                .correlationId(correlationId)
                .invalidParameters(invalidParams)
                .build();

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(problem)
                .build();
    }

    private String extractFieldName(Path propertyPath) {
        if (propertyPath == null) {
            return "unknown";
        }
        String fieldName = "unknown";
        for (Path.Node node : propertyPath) {
            fieldName = node.getName();
        }
        return fieldName;
    }
}
