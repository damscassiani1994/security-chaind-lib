package co.com.scl.security.interfaces;

import org.springframework.security.core.Authentication;

public interface IJWTProvider {

    String getUsername(String token);
    boolean validateToken(String token);
    String tokenGenerate(Authentication authentication);
}
