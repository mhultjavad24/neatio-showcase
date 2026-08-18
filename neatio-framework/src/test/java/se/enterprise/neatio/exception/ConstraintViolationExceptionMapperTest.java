package se.enterprise.neatio.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.neatio.filter.CorrelationContext;
import se.enterprise.neatio.model.ProblemDetail;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConstraintViolationExceptionMapperTest {

    private ConstraintViolationExceptionMapper mapper;
    private CorrelationContext correlationContext;
    private UriInfo uriInfo;

    @BeforeEach
    void setUp() throws Exception {
        mapper = new ConstraintViolationExceptionMapper();
        correlationContext = new CorrelationContext();
        correlationContext.setCorrelationId("validation-cid-111");
        uriInfo = mock(UriInfo.class);
        when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:9080/api/v1/kudos"));

        var cidField = ConstraintViolationExceptionMapper.class.getDeclaredField("correlationContext");
        cidField.setAccessible(true);
        cidField.set(mapper, correlationContext);

        var uriField = ConstraintViolationExceptionMapper.class.getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(mapper, uriInfo);
    }

    @Test
    void shouldMapConstraintViolationExceptionTo400ProblemDetail() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        Path.Node node = mock(Path.Node.class);

        when(node.getName()).thenReturn("amount");
        Iterator<Path.Node> iterator = Collections.singletonList(node).iterator();
        when(path.iterator()).thenReturn(iterator);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getInvalidValue()).thenReturn(0);
        when(violation.getMessage()).thenReturn("Kudos transfer must be at least 1 credit.");

        Set<ConstraintViolation<?>> violations = Collections.singleton(violation);
        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", violations);

        Response response = mapper.toResponse(exception);

        assertThat(response.getStatus()).isEqualTo(400);
        ProblemDetail problem = (ProblemDetail) response.getEntity();
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
        assertThat(problem.getCorrelationId()).isEqualTo("validation-cid-111");
        assertThat(problem.getInvalidParameters()).hasSize(1);
        assertThat(problem.getInvalidParameters().get(0).field()).isEqualTo("amount");
        assertThat(problem.getInvalidParameters().get(0).rejectedValue()).isEqualTo(0);
        assertThat(problem.getInvalidParameters().get(0).message()).isEqualTo("Kudos transfer must be at least 1 credit.");
    }
}
