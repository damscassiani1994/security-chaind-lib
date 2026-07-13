package co.com.scl.repository;

import co.com.scl.model.UserAuthModel;

import java.util.Optional;

/**
 * Strategy interface for user authentication data access.
 * Each database implementation provides its own concrete class.
 */
public interface IUserAuthRepository {
    Optional<UserAuthModel> findByUsername(String username);
}