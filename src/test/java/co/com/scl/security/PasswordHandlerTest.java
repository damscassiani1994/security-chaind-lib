package co.com.scl.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordHandlerTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void generatePasswordDelegatesToEncoder() {
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        PasswordHandler handler = new PasswordHandler(passwordEncoder);

        String result = handler.generatePassword("raw-password");

        assertThat(result).isEqualTo("encoded-password");
        verify(passwordEncoder).encode("raw-password");
    }

    @Test
    void verifyPasswordReturnsTrueWhenEncoderMatches() {
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        PasswordHandler handler = new PasswordHandler(passwordEncoder);

        Boolean result = handler.verifyPassword("raw-password", "encoded-password");

        assertThat(result).isTrue();
    }

    @Test
    void verifyPasswordReturnsFalseWhenEncoderDoesNotMatch() {
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);
        PasswordHandler handler = new PasswordHandler(passwordEncoder);

        Boolean result = handler.verifyPassword("wrong-password", "encoded-password");

        assertThat(result).isFalse();
    }
}
