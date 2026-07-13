package co.com.scl.repository.strategy;

import co.com.scl.config.SecurityProperties;

public class SqlServerUserAuthStrategy extends AbstractSqlUserAuthStrategy {

    public SqlServerUserAuthStrategy(SecurityProperties properties) {
        super(properties, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }
}