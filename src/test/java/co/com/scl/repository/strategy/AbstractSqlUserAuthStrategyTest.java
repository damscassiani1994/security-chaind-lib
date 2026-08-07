package co.com.scl.repository.strategy;

import co.com.scl.config.SecurityProperties;
import co.com.scl.model.UserAuthModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractSqlUserAuthStrategyTest {

    private String jdbcUrl;
    private SecurityProperties properties;

    @BeforeEach
    void setUp() throws Exception {
        jdbcUrl = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE users (
                    username  VARCHAR(50),
                    password  VARCHAR(100),
                    is_active BOOLEAN,
                    roles     VARCHAR(200)
                )
                """);
            statement.execute("INSERT INTO users VALUES ('john.doe', 'encoded-pw', TRUE, 'admin, user , ops')");
            statement.execute("INSERT INTO users VALUES ('inactive.user', 'pw', FALSE, NULL)");
            statement.execute("INSERT INTO users VALUES ('piped.user', 'pw', TRUE, 'admin|user')");
        }

        properties = new SecurityProperties();
        properties.getDatasource().setUrl(jdbcUrl);
        properties.getDatasource().setUsername("sa");
        properties.getDatasource().setPassword("");
    }

    @Test
    void findByUsernameReturnsUserWithTrimmedRolesWhenFound() {
        H2TestUserAuthStrategy strategy = new H2TestUserAuthStrategy(properties);

        Optional<UserAuthModel> result = strategy.findByUsername("john.doe");

        assertThat(result).isPresent();
        UserAuthModel user = result.get();
        assertThat(user.getUsername()).isEqualTo("john.doe");
        assertThat(user.getPassword()).isEqualTo("encoded-pw");
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRoles()).containsExactly("admin", "user", "ops");
    }

    @Test
    void findByUsernameReturnsEmptyWhenUserDoesNotExist() {
        H2TestUserAuthStrategy strategy = new H2TestUserAuthStrategy(properties);

        Optional<UserAuthModel> result = strategy.findByUsername("does.not.exist");

        assertThat(result).isEmpty();
    }

    @Test
    void findByUsernameReturnsEmptyRolesWhenRolesColumnIsNull() {
        H2TestUserAuthStrategy strategy = new H2TestUserAuthStrategy(properties);

        Optional<UserAuthModel> result = strategy.findByUsername("inactive.user");

        assertThat(result).isPresent();
        assertThat(result.get().isActive()).isFalse();
        assertThat(result.get().getRoles()).isEmpty();
    }

    @Test
    void findByUsernameHonorsACustomRolesSeparator() {
        properties.getUser().setRolesSeparator("|");
        H2TestUserAuthStrategy strategy = new H2TestUserAuthStrategy(properties);

        Optional<UserAuthModel> result = strategy.findByUsername("piped.user");

        assertThat(result).isPresent();
        assertThat(result.get().getRoles()).containsExactly("admin", "user");
    }
}
