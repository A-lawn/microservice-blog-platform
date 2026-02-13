package com.blog.platform.user.infrastructure.persistence.repository;

import com.blog.platform.common.domain.user.UserStatus;
import com.blog.platform.user.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserJpaRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserJpaRepository userRepository;
    
    private UserEntity testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new UserEntity("user-001", "testuser", "test@example.com", "hashedPassword");
        testUser.setNickname("Test User");
        testUser.setStatus(UserStatus.ACTIVE);
        entityManager.persistAndFlush(testUser);
    }
    
    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // When
        Optional<UserEntity> result = userRepository.findByUsername("testuser");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // When
        Optional<UserEntity> result = userRepository.findByUsername("nonexistent");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // When
        Optional<UserEntity> result = userRepository.findByEmail("test@example.com");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }
    
    @Test
    void findByUsernameOrEmail_ShouldReturnUser_WhenUsernameMatches() {
        // When
        Optional<UserEntity> result = userRepository.findByUsernameOrEmail("testuser");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }
    
    @Test
    void findByUsernameOrEmail_ShouldReturnUser_WhenEmailMatches() {
        // When
        Optional<UserEntity> result = userRepository.findByUsernameOrEmail("test@example.com");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    void existsByUsername_ShouldReturnTrue_WhenUserExists() {
        // When
        boolean exists = userRepository.existsByUsername("testuser");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    void existsByUsername_ShouldReturnFalse_WhenUserDoesNotExist() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");
        
        // Then
        assertThat(exists).isFalse();
    }
    
    @Test
    void existsByEmail_ShouldReturnTrue_WhenUserExists() {
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    void findByStatus_ShouldReturnActiveUsers() {
        // Given
        UserEntity inactiveUser = new UserEntity("user-002", "inactive", "inactive@example.com", "hashedPassword");
        inactiveUser.setStatus(UserStatus.INACTIVE);
        entityManager.persistAndFlush(inactiveUser);
        
        // When
        List<UserEntity> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        
        // Then
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
    
    @Test
    void findByStatusWithPagination_ShouldReturnPagedResults() {
        // Given
        for (int i = 2; i <= 5; i++) {
            UserEntity user = new UserEntity("user-00" + i, "user" + i, "user" + i + "@example.com", "hashedPassword");
            user.setStatus(UserStatus.ACTIVE);
            entityManager.persistAndFlush(user);
        }
        
        // When
        Page<UserEntity> result = userRepository.findByStatus(UserStatus.ACTIVE, PageRequest.of(0, 2));
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }
    
    @Test
    void findByCreatedAtAfter_ShouldReturnRecentUsers() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        
        // When
        List<UserEntity> recentUsers = userRepository.findByCreatedAtAfter(yesterday);
        
        // Then
        assertThat(recentUsers).hasSize(1);
        assertThat(recentUsers.get(0).getCreatedAt()).isAfter(yesterday);
    }
    
    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // Given
        UserEntity inactiveUser = new UserEntity("user-002", "inactive", "inactive@example.com", "hashedPassword");
        inactiveUser.setStatus(UserStatus.INACTIVE);
        entityManager.persistAndFlush(inactiveUser);
        
        // When
        long activeCount = userRepository.countByStatus(UserStatus.ACTIVE);
        long inactiveCount = userRepository.countByStatus(UserStatus.INACTIVE);
        
        // Then
        assertThat(activeCount).isEqualTo(1);
        assertThat(inactiveCount).isEqualTo(1);
    }
    
    @Test
    void searchActiveUsers_ShouldReturnMatchingUsers() {
        // Given
        UserEntity user2 = new UserEntity("user-002", "searchuser", "search@example.com", "hashedPassword");
        user2.setNickname("Search User");
        user2.setStatus(UserStatus.ACTIVE);
        entityManager.persistAndFlush(user2);
        
        Page<UserEntity> result = userRepository.findByKeyword("search", PageRequest.of(0, 10));
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("searchuser");
    }
}