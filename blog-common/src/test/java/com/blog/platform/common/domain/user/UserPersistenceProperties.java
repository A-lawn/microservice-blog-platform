package com.blog.platform.common.domain.user;

import com.blog.platform.common.domain.DomainEventPublisher;
import com.blog.platform.common.repository.BaseRepository;
import net.jqwik.api.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * **Feature: microservice-blog-platform, Property 4: 用户数据持久化往返**
 * **验证需求: Requirements 1.4**
 * 
 * 属性测试：验证用户数据持久化的往返一致性
 */
class UserPersistenceProperties {
    
    @Property(tries = 100)
    @Label("对于任何用户操作（注册或更新），操作完成后从数据库查询应当返回相同的用户数据")
    void userPersistenceRoundTrip(
            @ForAll("validUsernames") String username,
            @ForAll("validEmails") String email,
            @ForAll("validPasswords") String password,
            @ForAll("userProfiles") UserProfile profile) {
        
        TestUserRepository repository = new TestUserRepository();
        
        User originalUser = User.register(
            Username.of(username),
            Email.of(email),
            Password.fromRawPassword(password)
        );
        
        User savedUser = repository.save(originalUser);
        
        Optional<User> retrievedUser = repository.findById(savedUser.getId());
        
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getId()).isEqualTo(originalUser.getId());
        assertThat(retrievedUser.get().getUsername()).isEqualTo(originalUser.getUsername());
        assertThat(retrievedUser.get().getEmail()).isEqualTo(originalUser.getEmail());
        assertThat(retrievedUser.get().getPassword()).isEqualTo(originalUser.getPassword());
        assertThat(retrievedUser.get().getStatus()).isEqualTo(originalUser.getStatus());
        
        retrievedUser.get().updateProfile(profile);
        User updatedUser = repository.save(retrievedUser.get());
        
        Optional<User> finalUser = repository.findById(updatedUser.getId());
        
        assertThat(finalUser).isPresent();
        assertThat(finalUser.get().getProfile()).isEqualTo(profile);
        assertThat(finalUser.get().getProfile().getNickname()).isEqualTo(profile.getNickname());
        assertThat(finalUser.get().getProfile().getAvatar()).isEqualTo(profile.getAvatar());
        assertThat(finalUser.get().getProfile().getBio()).isEqualTo(profile.getBio());
    }
    
    @Property(tries = 100)
    @Label("对于任何用户，保存和查询操作应当保持数据完整性")
    void userDataIntegrityMaintained(
            @ForAll("validUsernames") String username,
            @ForAll("validEmails") String email,
            @ForAll("validPasswords") String password) {
        
        TestUserRepository repository = new TestUserRepository();
        
        User user = User.register(
            Username.of(username),
            Email.of(email),
            Password.fromRawPassword(password)
        );
        
        User savedUser = repository.save(user);
        
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername().getValue()).isEqualTo(username);
        assertThat(savedUser.getEmail().getValue()).isEqualTo(email);
        assertThat(savedUser.verifyPassword(password)).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        
        Optional<User> retrievedUser = repository.findById(savedUser.getId());
        
        assertThat(retrievedUser).isPresent();
        User retrieved = retrievedUser.get();
        
        assertThat(retrieved.getId()).isEqualTo(savedUser.getId());
        assertThat(retrieved.getUsername()).isEqualTo(savedUser.getUsername());
        assertThat(retrieved.getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(retrieved.getPassword()).isEqualTo(savedUser.getPassword());
        assertThat(retrieved.getStatus()).isEqualTo(savedUser.getStatus());
        assertThat(retrieved.getProfile()).isEqualTo(savedUser.getProfile());
        assertThat(retrieved.getCreatedAt()).isEqualTo(savedUser.getCreatedAt());
        assertThat(retrieved.getUpdatedAt()).isEqualTo(savedUser.getUpdatedAt());
    }
    
    @Property(tries = 100)
    @Label("对于任何用户状态变更，持久化应当正确保存状态")
    void userStatusPersistence(
            @ForAll("validUsernames") String username,
            @ForAll("validEmails") String email,
            @ForAll("validPasswords") String password) {
        
        TestUserRepository repository = new TestUserRepository();
        User user = User.register(
            Username.of(username),
            Email.of(email),
            Password.fromRawPassword(password)
        );
        
        User savedUser = repository.save(user);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        
        savedUser.deactivate();
        User deactivatedUser = repository.save(savedUser);
        
        Optional<User> retrievedUser = repository.findById(deactivatedUser.getId());
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getStatus()).isEqualTo(UserStatus.INACTIVE);
        
        retrievedUser.get().activate();
        User reactivatedUser = repository.save(retrievedUser.get());
        
        Optional<User> finalUser = repository.findById(reactivatedUser.getId());
        assertThat(finalUser).isPresent();
        assertThat(finalUser.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
    
    @Provide
    Arbitrary<String> validUsernames() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(3)
            .ofMaxLength(15)
            .map(s -> s + Arbitraries.integers().between(1, 99).sample());
    }
    
    @Provide
    Arbitrary<String> validEmails() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(3)
            .ofMaxLength(10)
            .map(localPart -> localPart + "@example.com");
    }
    
    @Provide
    Arbitrary<String> validPasswords() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(6)
            .ofMaxLength(20)
            .map(s -> s + "123");
    }
    
    @Provide
    Arbitrary<UserProfile> userProfiles() {
        return Arbitraries.of(
            UserProfile.empty(),
            UserProfile.of("测试昵称", "", ""),
            UserProfile.of("", "http://example.com/avatar.jpg", ""),
            UserProfile.of("", "", "这是一个测试简介"),
            UserProfile.of("完整昵称", "http://example.com/full.jpg", "完整的个人简介")
        );
    }
    
    static class TestUserRepository extends BaseRepository<User, UserId> {
        
        private final Map<UserId, User> storage = new HashMap<>();
        
        public TestUserRepository() {
            super(mock(DomainEventPublisher.class));
        }
        
        @Override
        protected User doSave(User user) {
            User persistedUser = User.reconstruct(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getProfile(),
                user.getStatus(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
            );
            
            storage.put(user.getId(), persistedUser);
            return persistedUser;
        }
        
        @Override
        protected void doDelete(User user) {
            storage.remove(user.getId());
        }
        
        @Override
        public Optional<User> findById(UserId id) {
            User user = storage.get(id);
            if (user == null) {
                return Optional.empty();
            }
            
            return Optional.of(User.reconstruct(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getProfile(),
                user.getStatus(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
            ));
        }
        
        @Override
        public boolean existsById(UserId id) {
            return storage.containsKey(id);
        }
        
        @Override
        public void deleteById(UserId id) {
            storage.remove(id);
        }
        
        @Override
        public List<User> findAll() {
            return storage.values().stream()
                .map(user -> User.reconstruct(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getProfile(),
                    user.getStatus(),
                    user.getRole(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
                ))
                .toList();
        }
        
        @Override
        public long count() {
            return storage.size();
        }
    }
}
