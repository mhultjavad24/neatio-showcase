package se.enterprise.neatio.interceptor;

import jakarta.interceptor.InvocationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.neatio.annotation.NeatioAudited;
import se.enterprise.neatio.filter.CorrelationContext;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NeatioAuditInterceptorTest {

    private NeatioAuditInterceptor interceptor;
    private CorrelationContext correlationContext;
    private InvocationContext invocationContext;

    static class SampleService {
        @NeatioAudited(action = "SAMPLE_ACTION")
        public String executeSuccess(String input) {
            return "Result: " + input;
        }

        @NeatioAudited(action = "FAILING_ACTION")
        public void executeFailure() {
            throw new IllegalStateException("Simulated failure");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        interceptor = new NeatioAuditInterceptor();
        correlationContext = new CorrelationContext();
        correlationContext.setCorrelationId("test-audit-cid-789");
        invocationContext = mock(InvocationContext.class);

        var field = NeatioAuditInterceptor.class.getDeclaredField("correlationContext");
        field.setAccessible(true);
        field.set(interceptor, correlationContext);
    }

    @Test
    void shouldAuditSuccessfulOperation() throws Exception {
        SampleService target = new SampleService();
        Method method = SampleService.class.getMethod("executeSuccess", String.class);

        when(invocationContext.getTarget()).thenReturn(target);
        when(invocationContext.getMethod()).thenReturn(method);
        when(invocationContext.getParameters()).thenReturn(new Object[]{"hello"});
        when(invocationContext.proceed()).thenReturn("Result: hello");

        Object result = interceptor.auditOperation(invocationContext);

        assertThat(result).isEqualTo("Result: hello");
        verify(invocationContext).proceed();
    }

    @Test
    void shouldAuditFailedOperationAndRethrow() throws Exception {
        SampleService target = new SampleService();
        Method method = SampleService.class.getMethod("executeFailure");

        when(invocationContext.getTarget()).thenReturn(target);
        when(invocationContext.getMethod()).thenReturn(method);
        when(invocationContext.getParameters()).thenReturn(new Object[]{});
        when(invocationContext.proceed()).thenThrow(new IllegalStateException("Simulated failure"));

        assertThatThrownBy(() -> interceptor.auditOperation(invocationContext))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Simulated failure");
    }
}
