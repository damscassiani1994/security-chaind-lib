package co.com.scl.repository.strategy;

import co.com.scl.config.SecurityProperties;

public class PostgreSqlUserAuthStrategy extends AbstractSqlUserAuthStrategy {

    public PostgreSqlUserAuthStrategy(SecurityProperties properties) {
        super(properties, "org.postgresql.Driver");
    }
}
