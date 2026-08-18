package se.enterprise.neatio.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ModelRecordsTest {

    @Test
    void shouldCreateAndVerifyInvalidParameterRecord() {
        InvalidParameter param = new InvalidParameter("senderId", "", "Sender is required");

        assertThat(param.field()).isEqualTo("senderId");
        assertThat(param.rejectedValue()).isEqualTo("");
        assertThat(param.message()).isEqualTo("Sender is required");
        assertThat(param.toString()).contains("senderId");
    }

    @Test
    void shouldCreateAndVerifyAuditEventRecord() {
        Instant now = Instant.now();
        AuditEvent event = new AuditEvent(
                "KUDOS_TRANSFER",
                "cid-999",
                now,
                15L,
                "SUCCESS",
                "KudosResource",
                "sendKudos",
                null
        );

        assertThat(event.action()).isEqualTo("KUDOS_TRANSFER");
        assertThat(event.correlationId()).isEqualTo("cid-999");
        assertThat(event.timestamp()).isEqualTo(now);
        assertThat(event.durationMs()).isEqualTo(15L);
        assertThat(event.status()).isEqualTo("SUCCESS");
        assertThat(event.targetClass()).isEqualTo("KudosResource");
        assertThat(event.targetMethod()).isEqualTo("sendKudos");
        assertThat(event.errorMessage()).isNull();
    }
}
