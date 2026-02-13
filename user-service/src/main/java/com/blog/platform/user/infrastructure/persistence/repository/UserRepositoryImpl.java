package com.blog.platform.user.infrastructure.persistence.repository;

import com.blog.platform.common.domain.user.*;
import com.blog.platform.common.domain.DomainEventPublisher;
import com.blog.platform.common.repository.BaseRepository;
import com.blog.platform.user.domain.repository.UserRepository;
import com.blog.platform.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储实现
 */
@Repository
public class UserRepositoryImpl extends BaseRepository<User, UserId> implements UserRepository {
    
    private final UserJpaRepository jpaRepository;
    
    public UserRepositoryImpl(UserJpaRepository jpaRepository, DomainEventPublisher eventPublisher) {
        super(eventPublisher);
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.getValue())
                .map(this::toDomain);
    }
    
    @Override
    public Optional<User> findByUsername(Username username) {
        return jpaRepository.findByUsername(username.getValue())
                .map(this::toDomain);
    }
    
    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.getValue())
                .map(this::toDomain);
    }
    
    @Override
    public boolean existsByUsername(Username username) {
        return jpaRepository.existsByUsername(username.getValue());
    }
    
    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.getValue());
    }
    
    @Override
    public boolean existsById(UserId id) {
        return jpaRepository.existsById(id.getValue());
    }
    
    @Override
    public void deleteById(UserId id) {
        jpaRepository.deleteById(id.getValue());
    }
    
    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    protected User doSave(User domain) {
        UserEntity entity = toEntity(domain);
        UserEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }
    
    @Override
    protected void doDelete(User domain) {
        jpaRepository.deleteById(domain.getId().getValue());
    }
    
    private UserEntity toEntity(User domain) {
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId().getValue());
        entity.setUsername(domain.getUsername().getValue());
        entity.setEmail(domain.getEmail().getValue());
        entity.setPasswordHash(domain.getPassword().getHashedValue());
        entity.setNickname(domain.getProfile().getNickname());
        entity.setAvatarUrl(domain.getProfile().getAvatar());
        entity.setBio(domain.getProfile().getBio());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        // 目前聚合内只维护主角色，持久化侧多角色表由其他组件维护
        return entity;
    }
    
    private User toDomain(UserEntity entity) {
        UserRole role = entity.getRoles() != null && !entity.getRoles().isEmpty()
                ? UserRole.of(entity.getRoles().iterator().next().getRoleName())
                : UserRole.defaultRole();
        
        return User.reconstruct(
            UserId.of(entity.getId()),
            Username.of(entity.getUsername()),
            Email.of(entity.getEmail()),
            Password.fromHashedPassword(entity.getPasswordHash()),
            UserProfile.of(entity.getNickname(), entity.getAvatarUrl(), entity.getBio()),
            entity.getStatus(),
            role,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    @Override
    public void recordLogin(UserId userId) {
        jpaRepository.findById(userId.getValue()).ifPresent(entity -> {
            entity.recordLogin();
            jpaRepository.save(entity);
        });
    }
}