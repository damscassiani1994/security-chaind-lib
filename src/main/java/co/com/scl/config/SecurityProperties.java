package co.com.scl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "security.chaind")
public class SecurityProperties {

    /**
     * Endpoints that do not require authentication.
     * Example: /api/v1/auth/login, /actuator/health
     */
    private List<String> publicEndpoints = new ArrayList<>();

    /**
     * Database type to use for user authentication.
     * Supported values: mongodb | postgresql | mysql | oracle | sqlserver
     */
    private String databaseType;

    private JwtProperties jwt = new JwtProperties();
    private DatasourceProperties datasource = new DatasourceProperties();
    private UserProperties user = new UserProperties();

    @Data
    public static class JwtProperties {
        /** Base64-encoded secret key used to sign and verify JWT tokens. */
        private String secretKey;
        /** Token validity in milliseconds. Default: 24 hours. */
        private long expiration = 86400000L;
    }

    @Data
    public static class DatasourceProperties {
        /**
         * For MongoDB  : full connection URI, e.g. mongodb://localhost:27017
         * For SQL DBs  : full JDBC URL,       e.g. jdbc:postgresql://localhost:5432/mydb
         */
        private String url;
        /** MongoDB only: database name (when not included in the URI). */
        private String database;
        private String username;
        private String password;
    }

    @Data
    public static class UserProperties {
        /** Collection name (MongoDB) or table name (SQL) that stores users. */
        private String collectionOrTable = "users";
        /** Field / column that holds the username. */
        private String usernameField = "username";
        /** Field / column that holds the hashed password. */
        private String passwordField = "password";
        /** Field / column that indicates whether the account is active. */
        private String isActiveField = "is_active";
        /** Field / column that holds the user's roles (SQL: single delimited string; MongoDB: string or array). */
        private String rolesField = "roles";
        /** Delimiter used to split roles when they are stored as a single string (SQL databases). */
        private String rolesSeparator = ",";
    }
}