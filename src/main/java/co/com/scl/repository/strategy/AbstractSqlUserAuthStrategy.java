package co.com.scl.repository.strategy;

import co.com.scl.config.SecurityProperties;
import co.com.scl.model.UserAuthModel;
import co.com.scl.repository.IUserAuthRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

public abstract class AbstractSqlUserAuthStrategy implements IUserAuthRepository {

    private final SecurityProperties properties;
    private final JdbcTemplate jdbcTemplate;

    protected AbstractSqlUserAuthStrategy(SecurityProperties properties, String driverClassName) {
        this.properties = properties;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getDatasource().getUrl());
        config.setUsername(properties.getDatasource().getUsername());
        config.setPassword(properties.getDatasource().getPassword());
        config.setDriverClassName(driverClassName);
        this.jdbcTemplate = new JdbcTemplate(new HikariDataSource(config));
    }

    @Override
    public Optional<UserAuthModel> findByUsername(String username) {
        SecurityProperties.UserProperties u = properties.getUser();
        String sql = String.format(
            "SELECT %s, %s, %s FROM %s WHERE %s = ?",
            u.getUsernameField(),
            u.getPasswordField(),
            u.getIsActiveField(),
            u.getCollectionOrTable(),
            u.getUsernameField()
        );

        try {
            UserAuthModel user = jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> UserAuthModel.builder()
                    .username(rs.getString(u.getUsernameField()))
                    .password(rs.getString(u.getPasswordField()))
                    .active(rs.getBoolean(u.getIsActiveField()))
                    .build(),
                username);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
