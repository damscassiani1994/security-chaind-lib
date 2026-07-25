package co.com.scl.security;

import co.com.scl.model.UserAuthModel;
import co.com.scl.repository.IUserAuthRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Set;

public class CustomUserDetailServices implements UserDetailsService {

    private final IUserAuthRepository userAuthRepository;

    public CustomUserDetailServices(IUserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAuthModel user = userAuthRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new User(
            user.getUsername(),
            user.getPassword(),
            user.isActive(),
            true, true, true,
            toAuthorities(user.getRoles())
        );
    }

    private List<GrantedAuthority> toAuthorities(Set<String> roles) {
        return roles.stream()
            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase())
            .map(SimpleGrantedAuthority::new)
            .map(GrantedAuthority.class::cast)
            .toList();
    }
}