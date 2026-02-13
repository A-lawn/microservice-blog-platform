package com.blog.platform.user.infrastructure.persistence.repository;

import com.blog.platform.user.infrastructure.persistence.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionJpaRepository extends JpaRepository<UserSessionEntity, Long> {
    
    /**
     * Find session by token hash
     */
    Optional<UserSessionEntity> findByTokenHash(String tokenHash);
    
    /**
     * Find sessions by user ID
     */
    List<UserSessionEntity> findByUserId(String userId);
    
    /**
     * Find active sessions by user ID
     */
    @Query("SELECT s FROM UserSessionEntity s WHERE s.userId = :userId AND s.expiresAt > :now")
    List<UserSessionEntity> findActiveSessionsByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);
    
    /**
     * Find expired sessions
     */
    @Query("SELECT s FROM UserSessionEntity s WHERE s.expiresAt <= :now")
    List<UserSessionEntity> findExpiredSessions(@Param("now") LocalDateTime now);
    
    /**
     * Delete expired sessions
     */
    @Modifying
    @Query("DELETE FROM UserSessionEntity s WHERE s.expiresAt <= :now")
    int deleteExpiredSessions(@Param("now") LocalDateTime now);
    
    /**
     * Delete sessions by user ID
     */
    @Modifying
    @Query("DELETE FROM UserSessionEntity s WHERE s.userId = :userId")
    int deleteByUserId(@Param("userId") String userId);
    
    /**
     * Delete session by token hash
     */
    @Modifying
    @Query("DELETE FROM UserSessionEntity s WHERE s.tokenHash = :tokenHash")
    int deleteByTokenHash(@Param("tokenHash") String tokenHash);
    
    /**
     * Count active sessions by user ID
     */
    @Query("SELECT COUNT(s) FROM UserSessionEntity s WHERE s.userId = :userId AND s.expiresAt > :now")
    long countActiveSessionsByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);
}