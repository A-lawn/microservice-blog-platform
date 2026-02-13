package com.blog.platform.user.infrastructure.persistence.repository;

import com.blog.platform.user.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    
    Page<NotificationEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    List<NotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);
    
    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") String userId);
    
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") String userId);
    
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);
}
