package se.enterprise.fikavault;

import jakarta.ws.rs.ApplicationPath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestAppTest {

    @Test
    void shouldHaveApplicationPathAnnotation() {
        RestApp app = new RestApp();
        assertThat(app).isNotNull();

        ApplicationPath annotation = RestApp.class.getAnnotation(ApplicationPath.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("/api/v1");
    }
}
