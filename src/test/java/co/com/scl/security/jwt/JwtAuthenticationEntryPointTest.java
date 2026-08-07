package co.com.scl.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private AuthenticationException authenticationException;

    @Test
    void commenceSendsUnauthorizedErrorWithExceptionMessage() throws Exception {
        org.mockito.Mockito.when(authenticationException.getMessage()).thenReturn("Full authentication is required");

        new JwtAuthenticationEntryPoint().commence(request, response, authenticationException);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Full authentication is required");
    }
}
