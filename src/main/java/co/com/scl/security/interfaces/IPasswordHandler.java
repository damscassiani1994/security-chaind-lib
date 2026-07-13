package co.com.scl.security.interfaces;

public interface IPasswordHandler {
    String generatePassword(String password);
    Boolean verifyPassword(String password, String encodedPassword);
}
