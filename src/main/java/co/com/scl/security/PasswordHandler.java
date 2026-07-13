package co.com.scl.security;

import co.com.scl.security.interfaces.IPasswordHandler;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHandler implements IPasswordHandler {

    private final PasswordEncoder passwordEncoder;

    public PasswordHandler(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String generatePassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public Boolean verifyPassword(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }
}