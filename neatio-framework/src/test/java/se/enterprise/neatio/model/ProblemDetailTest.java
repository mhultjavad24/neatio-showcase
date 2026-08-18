package se.enterprise.neatio.model;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemDetailTest {

    @Test
    void shouldConstructWithFactoryMethods() {
        ProblemDetail pd1 = ProblemDetail.forStatus(400);
        assertThat(pd1.getStatus()).isEqualTo(400);
        assertThat(pd1.getTimestamp()).isNotNull();

        ProblemDetail pd2 = ProblemDetail.forStatusAndDetail(422, "Insufficient funds");
        assertThat(pd2.getStatus()).isEqualTo(422);
        assertThat(pd2.getDetail()).isEqualTo("Insufficient funds");
    }

    @Test
    void shouldBuildCompleteProblemDetailViaBuilder() {
        Instant now = Instant.now();
        URI type = URI.create("https://neatio.internal/errors/test-error");
        URI instance = URI.create("/api/v1/test");

        InvalidParameter invalidParam = new InvalidParameter("fieldA", -1, "Must be positive");

        ProblemDetail problem = ProblemDetail.builder()
                .type(type)
                .title("Test Error")
                .status(400)
                .detail("A validation error occurred")
                .instance(instance)
                .correlationId("cid-abc-123")
                .timestamp(now)
                .invalidParameters(List.of(invalidParam))
                .property("customProp", "customVal")
                .build();

        assertThat(problem.getType()).isEqualTo(type);
        assertThat(problem.getTitle()).isEqualTo("Test Error");
        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).isEqualTo("A validation error occurred");
        assertThat(problem.getInstance()).isEqualTo(instance);
        assertThat(problem.getCorrelationId()).isEqualTo("cid-abc-123");
        assertThat(problem.getTimestamp()).isEqualTo(now);
        assertThat(problem.getInvalidParameters()).hasSize(1);
        assertThat(problem.getInvalidParameters().get(0).field()).isEqualTo("fieldA");
        assertThat(problem.getProperties()).containsEntry("customProp", "customVal");
    }

    @Test
    void shouldSupportFluentStringUrlsAndParameterAdditions() {
        ProblemDetail problem = ProblemDetail.builder()
                .type("https://neatio.internal/errors/string-type")
                .instance("/api/v1/resource")
                .addInvalidParameter("param1", "badValue", "Invalid value")
                .build();

        assertThat(problem.getType()).isEqualTo(URI.create("https://neatio.internal/errors/string-type"));
        assertThat(problem.getInstance()).isEqualTo(URI.create("/api/v1/resource"));
        assertThat(problem.getInvalidParameters()).hasSize(1);
        assertThat(problem.getInvalidParameters().get(0).field()).isEqualTo("param1");
    }

    @Test
    void shouldSetAndGetDirectProperties() {
        ProblemDetail pd = new ProblemDetail();
        pd.setType(URI.create("https://neatio.internal/test"));
        pd.setTitle("Title");
        pd.setStatus(500);
        pd.setDetail("Detail");
        pd.setInstance(URI.create("/instance"));
        pd.setCorrelationId("cid-1");
        Instant ts = Instant.now();
        pd.setTimestamp(ts);
        pd.setInvalidParameters(Collections.emptyList());
        pd.setProperties(Collections.singletonMap("key", "val"));

        assertThat(pd.getType()).isEqualTo(URI.create("https://neatio.internal/test"));
        assertThat(pd.getTitle()).isEqualTo("Title");
        assertThat(pd.getStatus()).isEqualTo(500);
        assertThat(pd.getDetail()).isEqualTo("Detail");
        assertThat(pd.getInstance()).isEqualTo(URI.create("/instance"));
        assertThat(pd.getCorrelationId()).isEqualTo("cid-1");
        assertThat(pd.getTimestamp()).isEqualTo(ts);
        assertThat(pd.getInvalidParameters()).isEmpty();
        assertThat(pd.getProperties()).containsEntry("key", "val");
    }
}
