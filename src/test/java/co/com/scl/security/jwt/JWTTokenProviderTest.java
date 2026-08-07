package co.com.scl.security.jwt;

import co.com.scl.config.SecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JWTTokenProviderTest {

    private static final String SECRET_KEY =
        Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes());

    private SecurityProperties properties;
    private JWTTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        properties = new SecurityProperties();
        properties.getJwt().setSecretKey(SECRET_KEY);
        properties.getJwt().setExpiration(60_000L);
        tokenProvider = new JWTTokenProvider(properties);
    }

    @Test
    void tokenGenerateProducesAWellFormedJwt() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("john.doe", null);

        String token = tokenProvider.tokenGenerate(authentication);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void getUsernameReturnsTheSubjectEncodedInTheToken() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("jane.doe", null);
        String token = tokenProvider.tokenGenerate(authentication);

        String username = tokenProvider.getUsername(token);

        assertThat(username).isEqualTo("jane.doe");
    }

    @Test
    void validateTokenReturnsTrueForAFreshlyGeneratedToken() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("john.doe", null);
        String token = tokenProvider.tokenGenerate(authentication);

        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateTokenReturnsFalseForAMalformedToken() {
        assertThat(tokenProvider.validateToken("not-a-valid-jwt")).isFalse();
    }

    @Test
    void validateTokenReturnsFalseForAnExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
        Date past = new Date(System.currentTimeMillis() - 10_000L);
        String expiredToken = Jwts.builder()
            .subject("john.doe")
            .issuedAt(new Date(past.getTime() - 1_000L))
            .expiration(past)
            .signWith(key)
            .compact();

        assertThat(tokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    void validateTokenReturnsFalseWhenSignedWithADifferentKey() {
        SecretKey otherKey = Keys.hmacShaKeyFor("98765432109876543210987654321098".getBytes());
        String token = Jwts.builder()
            .subject("john.doe")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 60_000L))
            .signWith(otherKey)
            .compact();

        assertThat(tokenProvider.validateToken(token)).isFalse();
    }
}
