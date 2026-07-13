package co.com.scl.repository.strategy;

import co.com.scl.config.SecurityProperties;

public class MySqlUserAuthStrategy extends AbstractSqlUserAuthStrategy {

    public MySqlUserAuthStrategy(SecurityProperties properties) {
        super(properties, "com.mysql.cj.jdbc.Driver");
    }
}