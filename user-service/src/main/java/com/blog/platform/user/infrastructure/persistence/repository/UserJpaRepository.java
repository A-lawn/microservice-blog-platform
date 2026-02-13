package com.blog.platform.user.infrastructure.persistence.repository;

import com.blog.platform.common.domain.user.UserStatus;
import com.blog.platform.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    
    /**
     * Find user by username
     */
    Optional<UserEntity> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<UserEntity> findByEmail(String email);
    
    /**
     * Find user by username or email
     */
    @Query("SELECT u FROM UserEntity u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<UserEntity> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by status
     */
    List<UserEntity> findByStatus(UserStatus status);
    
    /**
     * Find users by status with pagination
     */
    Page<UserEntity> findByStatus(UserStatus status, Pageable pageable);
    
    /**
     * Find users created after a specific date
     */
    List<UserEntity> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find users by nickname containing (case insensitive)
     */
    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :nickname, '%'))")
    List<UserEntity> findByNicknameContainingIgnoreCase(@Param("nickname") String nickname);
    
    /**
     * Count users by status
     */
    long countByStatus(UserStatus status);
    
    /**
     * Count users created after a specific date
     */
    long countByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find users with statistics
     */
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.statistics WHERE u.id = :userId")
    Optional<UserEntity> findByIdWithStatistics(@Param("userId") String userId);
    
    /**
     * Find users with roles
     */
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles WHERE u.id = :userId")
    Optional<UserEntity> findByIdWithRoles(@Param("userId") String userId);
    
    /**
     * Find active users with pagination
     */
    @Query("SELECT u FROM UserEntity u WHERE u.status = 'ACTIVE' ORDER BY u.createdAt DESC")
    Page<UserEntity> findActiveUsers(Pageable pageable);
    
    @Query("SELECT u FROM UserEntity u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<UserEntity> findByKeyword(@Param("searchTerm") String searchTerm, Pageable pageable);
}