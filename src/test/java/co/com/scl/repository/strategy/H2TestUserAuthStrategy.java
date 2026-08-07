package co.com.scl.repository.strategy;

import co.com.scl.config.SecurityProperties;

class H2TestUserAuthStrategy extends AbstractSqlUserAuthStrategy {

    H2TestUserAuthStrategy(SecurityProperties properties) {
        super(properties, "org.h2.Driver");
    }
}
