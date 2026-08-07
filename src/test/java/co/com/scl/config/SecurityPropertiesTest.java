package co.com.scl.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPropertiesTest {

    @Test
    void defaultsAreSetForOptionalFields() {
        SecurityProperties properties = new SecurityProperties();

        assertThat(properties.getPublicEndpoints()).isEmpty();
        assertThat(properties.getDatabaseType()).isNull();
        assertThat(properties.getJwt().getExpiration()).isEqualTo(86_400_000L);
        assertThat(properties.getJwt().getSecretKey()).isNull();
        assertThat(properties.getUser().getCollectionOrTable()).isEqualTo("users");
        assertThat(properties.getUser().getUsernameField()).isEqualTo("username");
        assertThat(properties.getUser().getPasswordField()).isEqualTo("password");
        assertThat(properties.getUser().getIsActiveField()).isEqualTo("is_active");
        assertThat(properties.getUser().getRolesField()).isEqualTo("roles");
        assertThat(properties.getUser().getRolesSeparator()).isEqualTo(",");
    }

    @Test
    void settersOverrideDefaultValues() {
        SecurityProperties properties = new SecurityProperties();

        properties.setDatabaseType("postgresql");
        properties.setPublicEndpoints(List.of("/api/v1/auth/login"));
        properties.getJwt().setSecretKey("secret");
        properties.getJwt().setExpiration(1_000L);
        properties.getDatasource().setUrl("jdbc:postgresql://localhost:5432/mydb");
        properties.getDatasource().setUsername("admin");
        properties.getDatasource().setPassword("pwd");
        properties.getDatasource().setDatabase("mydb");
        properties.getUser().setCollectionOrTable("app_users");
        properties.getUser().setRolesSeparator("|");

        assertThat(properties.getDatabaseType()).isEqualTo("postgresql");
        assertThat(properties.getPublicEndpoints()).containsExactly("/api/v1/auth/login");
        assertThat(properties.getJwt().getSecretKey()).isEqualTo("secret");
        assertThat(properties.getJwt().getExpiration()).isEqualTo(1_000L);
        assertThat(properties.getDatasource().getUrl()).isEqualTo("jdbc:postgresql://localhost:5432/mydb");
        assertThat(properties.getDatasource().getUsername()).isEqualTo("admin");
        assertThat(properties.getDatasource().getPassword()).isEqualTo("pwd");
        assertThat(properties.getDatasource().getDatabase()).isEqualTo("mydb");
        assertThat(properties.getUser().getCollectionOrTable()).isEqualTo("app_users");
        assertThat(properties.getUser().getRolesSeparator()).isEqualTo("|");
    }
}
