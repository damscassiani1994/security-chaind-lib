package co.com.scl.repository.strategy;

import co.com.scl.config.SecurityProperties;

public class OracleUserAuthStrategy extends AbstractSqlUserAuthStrategy {

    public OracleUserAuthStrategy(SecurityProperties properties) {
        super(properties, "oracle.jdbc.OracleDriver");
    }
}