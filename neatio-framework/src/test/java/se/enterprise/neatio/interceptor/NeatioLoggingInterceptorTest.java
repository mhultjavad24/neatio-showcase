package se.enterprise.neatio.interceptor;

import jakarta.interceptor.InvocationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.enterprise.neatio.annotation.NeatioLogged;
import se.enterprise.neatio.filter.CorrelationContext;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NeatioLoggingInterceptorTest {

    private NeatioLoggingInterceptor interceptor;
    private CorrelationContext correlationContext;
    private InvocationContext invocationContext;

    static class TargetBean {
        @NeatioLogged
        public String execute(String msg) {
            return "Echo: " + msg;
        }

        @NeatioLogged
        public void executeError() {
            throw new IllegalArgumentException("Invalid input");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        interceptor = new NeatioLoggingInterceptor();
        correlationContext = new CorrelationContext();
        correlationContext.setCorrelationId("log-cid-1234");
        invocationContext = mock(InvocationContext.class);

        var field = NeatioLoggingInterceptor.class.getDeclaredField("correlationContext");
        field.setAccessible(true);
        field.set(interceptor, correlationContext);
    }

    @Test
    void shouldLogMethodExecutionSuccessfully() throws Exception {
        TargetBean target = new TargetBean();
        Method method = TargetBean.class.getMethod("execute", String.class);

        when(invocationContext.getTarget()).thenReturn(target);
        when(invocationContext.getMethod()).thenReturn(method);
        when(invocationContext.getParameters()).thenReturn(new Object[]{"hello"});
        when(invocationContext.proceed()).thenReturn("Echo: hello");

        Object result = interceptor.logMethodInvocation(invocationContext);

        assertThat(result).isEqualTo("Echo: hello");
        verify(invocationContext).proceed();
    }

    @Test
    void shouldLogExceptionAndRethrow() throws Exception {
        TargetBean target = new TargetBean();
        Method method = TargetBean.class.getMethod("executeError");

        when(invocationContext.getTarget()).thenReturn(target);
        when(invocationContext.getMethod()).thenReturn(method);
        when(invocationContext.getParameters()).thenReturn(new Object[]{});
        when(invocationContext.proceed()).thenThrow(new IllegalArgumentException("Invalid input"));

        assertThatThrownBy(() -> interceptor.logMethodInvocation(invocationContext))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid input");
    }
}
