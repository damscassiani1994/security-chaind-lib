package co.com.scl.security.jwt;

import co.com.scl.config.SecurityProperties;
import co.com.scl.security.interfaces.IJWTProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.util.Date;

public class JWTTokenProvider implements IJWTProvider {

    private final SecurityProperties properties;

    public JWTTokenProvider(SecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public String tokenGenerate(Authentication authentication) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getJwt().getExpiration());

        return Jwts.builder()
            .subject(authentication.getName())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(getKey())
            .compact();
    }

    @Override
    public String getUsername(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getJwt().getSecretKey()));
    }
}
