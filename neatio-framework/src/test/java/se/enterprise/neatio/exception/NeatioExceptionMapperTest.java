package se.enterprise.neatio.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.neatio.filter.CorrelationContext;
import se.enterprise.neatio.model.ProblemDetail;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NeatioExceptionMapperTest {

    private NeatioExceptionMapper mapper;
    private CorrelationContext correlationContext;
    private UriInfo uriInfo;

    @BeforeEach
    void setUp() throws Exception {
        mapper = new NeatioExceptionMapper();
        correlationContext = new CorrelationContext();
        correlationContext.setCorrelationId("test-cid-999");
        uriInfo = mock(UriInfo.class);
        when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:9080/api/v1/kudos"));

        var cidField = NeatioExceptionMapper.class.getDeclaredField("correlationContext");
        cidField.setAccessible(true);
        cidField.set(mapper, correlationContext);

        var uriField = NeatioExceptionMapper.class.getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(mapper, uriInfo);
    }

    @Test
    void shouldMapBusinessRuleExceptionToProblemDetail() {
        BusinessRuleException ex = new BusinessRuleException("Balance too low");
        ex.withExtension("employeeId", "SE-9821");

        Response response = mapper.toResponse(ex);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);

        ProblemDetail entity = (ProblemDetail) response.getEntity();
        assertThat(entity).isNotNull();
        assertThat(entity.getStatus()).isEqualTo(422);
        assertThat(entity.getTitle()).isEqualTo("Business Rule Violation");
        assertThat(entity.getDetail()).isEqualTo("Balance too low");
        assertThat(entity.getInstance()).isEqualTo(URI.create("/api/v1/kudos"));
        assertThat(entity.getCorrelationId()).isEqualTo("test-cid-999");
        assertThat(entity.getProperties()).containsEntry("employeeId", "SE-9821");
    }

    @Test
    void shouldMapWebApplicationException() {
        WebApplicationException ex = new WebApplicationException("Unauthorized", Response.Status.UNAUTHORIZED);

        Response response = mapper.toResponse(ex);

        assertThat(response.getStatus()).isEqualTo(401);
        ProblemDetail entity = (ProblemDetail) response.getEntity();
        assertThat(entity.getStatus()).isEqualTo(401);
        assertThat(entity.getTitle()).isEqualTo("Unauthorized");
        assertThat(entity.getCorrelationId()).isEqualTo("test-cid-999");
    }

    @Test
    void shouldMapUnexpectedThrowableTo500() {
        RuntimeException ex = new NullPointerException("Null pointer reference");

        Response response = mapper.toResponse(ex);

        assertThat(response.getStatus()).isEqualTo(500);
        ProblemDetail entity = (ProblemDetail) response.getEntity();
        assertThat(entity.getStatus()).isEqualTo(500);
        assertThat(entity.getTitle()).isEqualTo("Internal Server Error");
        assertThat(entity.getCorrelationId()).isEqualTo("test-cid-999");
    }
}
