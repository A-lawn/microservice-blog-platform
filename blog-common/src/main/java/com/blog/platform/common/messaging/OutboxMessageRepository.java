package com.blog.platform.common.messaging;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, String> {
    
    @Query("SELECT o FROM OutboxMessage o WHERE o.status IN ('PENDING', 'FAILED') " +
           "AND (o.nextRetryAt IS NULL OR o.nextRetryAt <= :now) " +
           "AND o.retryCount < o.maxRetry ORDER BY o.createdAt ASC")
    List<OutboxMessage> findPendingMessages(LocalDateTime now, Pageable pageable);
    
    @Query("SELECT o FROM OutboxMessage o WHERE o.status = 'DEAD_LETTER' ORDER BY o.createdAt DESC")
    List<OutboxMessage> findDeadLetterMessages(Pageable pageable);
    
    @Modifying
    @Query("UPDATE OutboxMessage o SET o.status = 'SENT', o.sentAt = :sentAt WHERE o.id = :id")
    void markAsSent(String id, LocalDateTime sentAt);
    
    @Modifying
    @Query("DELETE FROM OutboxMessage o WHERE o.status = 'SENT' AND o.sentAt < :before")
    int deleteSentMessagesBefore(LocalDateTime before);
    
    @Query("SELECT COUNT(o) FROM OutboxMessage o WHERE o.status IN ('PENDING', 'PROCESSING', 'FAILED')")
    long countPendingMessages();
    
    @Query("SELECT COUNT(o) FROM OutboxMessage o WHERE o.status = 'DEAD_LETTER'")
    long countDeadLetterMessages();
}
