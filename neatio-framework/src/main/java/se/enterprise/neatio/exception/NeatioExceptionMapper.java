package se.enterprise.neatio.exception;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.enterprise.neatio.filter.CorrelationContext;
import se.enterprise.neatio.model.ProblemDetail;

import java.net.URI;
import java.util.Map;

/**
 * Universal JAX-RS ExceptionMapper translating uncaught exceptions,
 * NeatioExceptions, and WebApplicationExceptions into RFC 7807 Problem Details.
 */
@Provider
@Priority(Priorities.USER)
public class NeatioExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = LoggerFactory.getLogger(NeatioExceptionMapper.class);

    private static final URI DEFAULT_INTERNAL_ERROR_TYPE = URI.create("https://neatio.internal/errors/internal-server-error");
    private static final String DEFAULT_INTERNAL_ERROR_TITLE = "Internal Server Error";
    private static final String DEFAULT_INTERNAL_ERROR_DETAIL = "An unexpected error occurred while processing the request.";

    @Context
    private UriInfo uriInfo;

    @Inject
    private CorrelationContext correlationContext;

    @Override
    public Response toResponse(Throwable exception) {
        String correlationId = correlationContext != null ? correlationContext.getCorrelationId() : "N/A";
        String requestPath = uriInfo != null ? uriInfo.getRequestUri().getPath() : "/unknown";

        if (exception instanceof NeatioException neatioEx) {
            log.warn("[NEATIO_EXCEPTION] correlationId={} status={} title='{}' detail='{}'",
                    correlationId, neatioEx.getStatus(), neatioEx.getTitle(), neatioEx.getDetail());

            ProblemDetail.Builder builder = ProblemDetail.builder()
                    .type(neatioEx.getType() != null ? neatioEx.getType() : URI.create("https://neatio.internal/errors/generic-error"))
                    .title(neatioEx.getTitle() != null ? neatioEx.getTitle() : "Business Error")
                    .status(neatioEx.getStatus())
                    .detail(neatioEx.getDetail())
                    .instance(requestPath)
                    .correlationId(correlationId);

            for (Map.Entry<String, Object> entry : neatioEx.getExtensions().entrySet()) {
                builder.property(entry.getKey(), entry.getValue());
            }

            return Response.status(neatioEx.getStatus())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(builder.build())
                    .build();
        }

        if (exception instanceof WebApplicationException webAppEx) {
            int status = webAppEx.getResponse().getStatus();
            Response.Status statusEnum = Response.Status.fromStatusCode(status);
            String title = statusEnum != null ? statusEnum.getReasonPhrase() : "HTTP Error " + status;

            log.info("[HTTP_EXCEPTION] correlationId={} status={} message='{}'",
                    correlationId, status, webAppEx.getMessage());

            ProblemDetail problem = ProblemDetail.builder()
                    .type("https://neatio.internal/errors/http-" + status)
                    .title(title)
                    .status(status)
                    .detail(webAppEx.getMessage())
                    .instance(requestPath)
                    .correlationId(correlationId)
                    .build();

            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(problem)
                    .build();
        }

        // Uncaught unexpected Throwable
        log.error("[UNHANDLED_EXCEPTION] correlationId={} error='{}'", correlationId, exception.getMessage(), exception);

        ProblemDetail problem = ProblemDetail.builder()
                .type(DEFAULT_INTERNAL_ERROR_TYPE)
                .title(DEFAULT_INTERNAL_ERROR_TITLE)
                .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .detail(DEFAULT_INTERNAL_ERROR_DETAIL)
                .instance(requestPath)
                .correlationId(correlationId)
                .build();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(problem)
                .build();
    }
}
