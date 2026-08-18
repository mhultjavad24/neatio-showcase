package se.enterprise.neatio.exception;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NeatioExceptionTest {

    @Test
    void shouldConstructNeatioExceptionWithAllVariants() {
        NeatioException ex1 = new NeatioException(400, "Bad Request", "Detail message");
        assertThat(ex1.getStatus()).isEqualTo(400);
        assertThat(ex1.getTitle()).isEqualTo("Bad Request");
        assertThat(ex1.getDetail()).isEqualTo("Detail message");
        assertThat(ex1.getType()).isNull();

        NeatioException ex2 = new NeatioException(404, "https://neatio.internal/errors/not-found", "Not Found", "Item not found");
        assertThat(ex2.getStatus()).isEqualTo(404);
        assertThat(ex2.getType()).isEqualTo(URI.create("https://neatio.internal/errors/not-found"));
        assertThat(ex2.getTitle()).isEqualTo("Not Found");

        NeatioException ex3 = new NeatioException(422, URI.create("https://neatio.internal/errors/business"), "Unprocessable", "Detail");
        ex3.withExtension("field", "val");
        assertThat(ex3.getExtensions()).containsEntry("field", "val");

        assertThatThrownBy(() -> ex3.getExtensions().put("another", "val"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldConstructBusinessRuleExceptionWithDefaults() {
        BusinessRuleException ex = new BusinessRuleException("Rule violation occurred");
        assertThat(ex.getStatus()).isEqualTo(422);
        assertThat(ex.getTitle()).isEqualTo("Business Rule Violation");
        assertThat(ex.getType()).isEqualTo(URI.create("https://neatio.internal/errors/business-rule-violation"));
        assertThat(ex.getDetail()).isEqualTo("Rule violation occurred");

        BusinessRuleException exCustom = new BusinessRuleException("https://neatio.internal/errors/custom", "Custom Rule", "Custom detail");
        assertThat(exCustom.getStatus()).isEqualTo(422);
        assertThat(exCustom.getTitle()).isEqualTo("Custom Rule");
    }

    @Test
    void shouldConstructResourceNotFoundExceptionWithDefaults() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User 123 not found");
        assertThat(ex.getStatus()).isEqualTo(404);
        assertThat(ex.getTitle()).isEqualTo("Resource Not Found");
        assertThat(ex.getType()).isEqualTo(URI.create("https://neatio.internal/errors/resource-not-found"));
        assertThat(ex.getDetail()).isEqualTo("User 123 not found");

        ResourceNotFoundException exCustom = new ResourceNotFoundException("https://neatio.internal/errors/user-missing", "Missing User", "Detail");
        assertThat(exCustom.getStatus()).isEqualTo(404);
        assertThat(exCustom.getTitle()).isEqualTo("Missing User");
    }
}
