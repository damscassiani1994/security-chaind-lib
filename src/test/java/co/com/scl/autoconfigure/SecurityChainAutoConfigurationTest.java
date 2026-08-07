package co.com.scl.autoconfigure;

import co.com.scl.config.SecurityProperties;
import co.com.scl.model.UserAuthModel;
import co.com.scl.repository.IUserAuthRepository;
import co.com.scl.security.PasswordHandler;
import co.com.scl.security.interfaces.IJWTProvider;
import co.com.scl.security.jwt.JWTAuthorizationFilter;
import co.com.scl.security.jwt.JWTTokenProvider;
import co.com.scl.security.jwt.JwtAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityChainAutoConfigurationTest {

    private final SecurityChainAutoConfiguration autoConfiguration = new SecurityChainAutoConfiguration();

    @Mock
    private IUserAuthRepository userAuthRepository;
    @Mock
    private IJWTProvider jwtTokenProvider;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @Test
    void passwordEncoderEncodesWithBcryptAndCanVerifyItsOwnOutput() {
        PasswordEncoder encoder = autoConfiguration.passwordEncoder();

        String encoded = encoder.encode("raw-password");

        assertThat(encoded).startsWith("{bcrypt}");
        assertThat(encoder.matches("raw-password", encoded)).isTrue();
        assertThat(encoder.matches("wrong-password", encoded)).isFalse();
    }

    @Test
    void passwordHandlerDelegatesToTheGivenEncoder() {
        PasswordEncoder encoder = autoConfiguration.passwordEncoder();
        PasswordHandler handler = autoConfiguration.passwordHandler(encoder);

        String encoded = handler.generatePassword("raw-password");

        assertThat(handler.verifyPassword("raw-password", encoded)).isTrue();
    }

    @Test
    void jwtTokenProviderIsWiredWithGivenProperties() {
        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setSecretKey(
            Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes()));
        properties.getJwt().setExpiration(60_000L);

        JWTTokenProvider provider = autoConfiguration.jwtTokenProvider(properties);
        Authentication authentication = new UsernamePasswordAuthenticationToken("john.doe", null);
        String token = provider.tokenGenerate(authentication);

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsername(token)).isEqualTo("john.doe");
    }

    @Test
    void jwtAuthenticationEntryPointBeanIsCreated() {
        assertThat(autoConfiguration.jwtAuthenticationEntryPoint())
            .isInstanceOf(JwtAuthenticationEntryPoint.class);
    }

    @Test
    void jwtAuthorizationFilterBeanIsWiredWithGivenCollaborators() throws Exception {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername("john.doe").password("pw").authorities("ROLE_ADMIN").build();
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUsername("valid-token")).thenReturn("john.doe");
        when(userDetailsService.loadUserByUsername("john.doe")).thenReturn(userDetails);

        JWTAuthorizationFilter filter = autoConfiguration.jwtAuthorizationFilter(jwtTokenProvider, userDetailsService);
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void customUserDetailsServiceBeanIsWiredWithGivenRepository() {
        UserAuthModel user = UserAuthModel.builder()
            .username("john.doe").password("pw").active(true).roles(Set.of("admin")).build();
        when(userAuthRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        UserDetailsService service = autoConfiguration.customUserDetailsService(userAuthRepository);

        assertThat(service.loadUserByUsername("john.doe").getUsername()).isEqualTo("john.doe");
    }

    @Test
    void customUserDetailsServiceBeanPropagatesNotFound() {
        when(userAuthRepository.findByUsername("missing")).thenReturn(Optional.empty());

        UserDetailsService service = autoConfiguration.customUserDetailsService(userAuthRepository);

        assertThatExceptionOfType(UsernameNotFoundException.class)
            .isThrownBy(() -> service.loadUserByUsername("missing"));
    }
}
