package co.com.scl.security;

import co.com.scl.model.UserAuthModel;
import co.com.scl.repository.IUserAuthRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;

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
            Collections.emptySet()
        );
    }
}