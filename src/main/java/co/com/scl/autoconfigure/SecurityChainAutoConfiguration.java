package co.com.scl.autoconfigure;

import co.com.scl.config.SecurityProperties;
import co.com.scl.repository.IUserAuthRepository;
import co.com.scl.repository.strategy.*;
import co.com.scl.security.CustomUserDetailServices;
import co.com.scl.security.PasswordHandler;
import co.com.scl.security.interfaces.IJWTProvider;
import co.com.scl.security.jwt.JWTAuthorizationFilter;
import co.com.scl.security.jwt.JWTTokenProvider;
import co.com.scl.security.jwt.JwtAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

@AutoConfiguration(before = SecurityAutoConfiguration.class)
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityChainAutoConfiguration {

    // ── Database Strategy Selection (Pattern Strategy) ────────────────────────

    @Bean
    @ConditionalOnMissingBean(IUserAuthRepository.class)
    @ConditionalOnProperty(name = "security.chaind.database-type", havingValue = "mongodb")
    @ConditionalOnClass(name = "com.mongodb.client.MongoClient")
    public IUserAuthRepository mongoUserAuthRepository(SecurityProperties properties) {
        return new MongoUserAuthStrategy(properties);
    }

    @Bean
    @ConditionalOnMissingBean(IUserAuthRepository.class)
    @ConditionalOnProperty(name = "security.chaind.database-type", havingValue = "postgresql")
    @ConditionalOnClass(name = "org.postgresql.Driver")
    public IUserAuthRepository postgresqlUserAuthRepository(SecurityProperties properties) {
        return new PostgreSqlUserAuthStrategy(properties);
    }

    @Bean
    @ConditionalOnMissingBean(IUserAuthRepository.class)
    @ConditionalOnProperty(name = "security.chaind.database-type", havingValue = "mysql")
    @ConditionalOnClass(name = "com.mysql.cj.jdbc.Driver")
    public IUserAuthRepository mysqlUserAuthRepository(SecurityProperties properties) {
        return new MySqlUserAuthStrategy(properties);
    }

    @Bean
    @ConditionalOnMissingBean(IUserAuthRepository.class)
    @ConditionalOnProperty(name = "security.chaind.database-type", havingValue = "oracle")
    @ConditionalOnClass(name = "oracle.jdbc.OracleDriver")
    public IUserAuthRepository oracleUserAuthRepository(SecurityProperties properties) {
        return new OracleUserAuthStrategy(properties);
    }

    @Bean
    @ConditionalOnMissingBean(IUserAuthRepository.class)
    @ConditionalOnProperty(name = "security.chaind.database-type", havingValue = "sqlserver")
    @ConditionalOnClass(name = "com.microsoft.sqlserver.jdbc.SQLServerDriver")
    public IUserAuthRepository sqlServerUserAuthRepository(SecurityProperties properties) {
        return new SqlServerUserAuthStrategy(properties);
    }

    // ── JWT ───────────────────────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    public JWTTokenProvider jwtTokenProvider(SecurityProperties properties) {
        return new JWTTokenProvider(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    @Bean
    @ConditionalOnMissingBean
    public JWTAuthorizationFilter jwtAuthorizationFilter(IJWTProvider jwtTokenProvider,
                                                          UserDetailsService userDetailsService) {
        return new JWTAuthorizationFilter(jwtTokenProvider, userDetailsService);
    }

    // ── UserDetailsService ────────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService customUserDetailsService(IUserAuthRepository userAuthRepository) {
        return new CustomUserDetailServices(userAuthRepository);
    }

    // ── Password Encoding ─────────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("pbkdf2@SpringSecurity_v5_8", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("scrypt@SpringSecurity_v5_8", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("argon2@SpringSecurity_v5_8", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        return new DelegatingPasswordEncoder("bcrypt", encoders);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordHandler passwordHandler(PasswordEncoder passwordEncoder) {
        return new PasswordHandler(passwordEncoder);
    }

    // ── Authentication Manager ────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── Security Filter Chain ─────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    SecurityProperties securityProperties,
                                                    JwtAuthenticationEntryPoint entryPoint,
                                                    JWTAuthorizationFilter jwtFilter) throws Exception {
        String[] publicEndpoints = securityProperties.getPublicEndpoints().toArray(new String[0]);

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                if (publicEndpoints.length > 0) {
                    auth.requestMatchers(publicEndpoints).permitAll();
                }
                auth.anyRequest().authenticated();
            })
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}