package co.com.scl.security;

import co.com.scl.model.UserAuthModel;
import co.com.scl.repository.IUserAuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServicesTest {

    @Mock
    private IUserAuthRepository userAuthRepository;

    @Test
    void loadUserByUsernameReturnsUserDetailsWhenFound() {
        UserAuthModel user = UserAuthModel.builder()
            .username("john.doe")
            .password("encoded-password")
            .active(true)
            .roles(Set.of("admin"))
            .build();
        when(userAuthRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        UserDetails result = new CustomUserDetailServices(userAuthRepository).loadUserByUsername("john.doe");

        assertThat(result.getUsername()).isEqualTo("john.doe");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getAuthorities())
            .extracting(Object::toString)
            .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsernameThrowsWhenUserNotFound() {
        when(userAuthRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatExceptionOfType(UsernameNotFoundException.class)
            .isThrownBy(() -> new CustomUserDetailServices(userAuthRepository).loadUserByUsername("unknown"))
            .withMessageContaining("unknown");
    }

    @Test
    void loadUserByUsernameDisablesInactiveUsers() {
        UserAuthModel user = UserAuthModel.builder()
            .username("jane.doe")
            .password("secret")
            .active(false)
            .roles(Set.of())
            .build();
        when(userAuthRepository.findByUsername("jane.doe")).thenReturn(Optional.of(user));

        UserDetails result = new CustomUserDetailServices(userAuthRepository).loadUserByUsername("jane.doe");

        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getAuthorities()).isEmpty();
    }

    @Test
    void loadUserByUsernameKeepsAlreadyPrefixedRolesUnchanged() {
        Set<String> roles = new LinkedHashSet<>();
        roles.add("ROLE_manager");
        UserAuthModel user = UserAuthModel.builder()
            .username("boss")
            .password("secret")
            .active(true)
            .roles(roles)
            .build();
        when(userAuthRepository.findByUsername("boss")).thenReturn(Optional.of(user));

        UserDetails result = new CustomUserDetailServices(userAuthRepository).loadUserByUsername("boss");

        assertThat(result.getAuthorities())
            .extracting(Object::toString)
            .containsExactly("ROLE_manager");
    }

    @Test
    void loadUserByUsernameUppercasesAndPrefixesPlainRoles() {
        Set<String> roles = new LinkedHashSet<>();
        roles.add("editor");
        roles.add("viewer");
        UserAuthModel user = UserAuthModel.builder()
            .username("multi")
            .password("secret")
            .active(true)
            .roles(roles)
            .build();
        when(userAuthRepository.findByUsername("multi")).thenReturn(Optional.of(user));

        UserDetails result = new CustomUserDetailServices(userAuthRepository).loadUserByUsername("multi");

        assertThat(result.getAuthorities())
            .extracting(Object::toString)
            .containsExactlyInAnyOrder("ROLE_EDITOR", "ROLE_VIEWER");
    }
}
