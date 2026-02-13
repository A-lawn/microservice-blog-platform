package com.blog.platform.user.domain.repository;

import com.blog.platform.common.domain.user.User;
import com.blog.platform.common.domain.user.UserId;
import com.blog.platform.common.domain.user.Username;
import com.blog.platform.common.domain.user.Email;
import com.blog.platform.common.repository.Repository;

import java.util.Optional;

public interface UserRepository extends Repository<User, UserId> {
    
    Optional<User> findByUsername(Username username);
    
    Optional<User> findByEmail(Email email);
    
    boolean existsByUsername(Username username);
    
    boolean existsByEmail(Email email);
    
    void recordLogin(UserId userId);
}