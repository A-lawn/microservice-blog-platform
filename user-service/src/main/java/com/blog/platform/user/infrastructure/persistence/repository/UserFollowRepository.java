package com.blog.platform.user.infrastructure.persistence.repository;

import com.blog.platform.user.infrastructure.persistence.entity.UserFollowEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollowEntity, Long> {
    
    Optional<UserFollowEntity> findByFollowerIdAndFollowingId(String followerId, String followingId);
    
    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);
    
    @Modifying
    @Query("DELETE FROM UserFollowEntity uf WHERE uf.followerId = :followerId AND uf.followingId = :followingId")
    void deleteByFollowerIdAndFollowingId(@Param("followerId") String followerId, @Param("followingId") String followingId);
    
    @Query("SELECT uf FROM UserFollowEntity uf WHERE uf.followerId = :followerId ORDER BY uf.createdAt DESC")
    Page<UserFollowEntity> findByFollowerId(@Param("followerId") String followerId, Pageable pageable);
    
    @Query("SELECT uf FROM UserFollowEntity uf WHERE uf.followingId = :followingId ORDER BY uf.createdAt DESC")
    Page<UserFollowEntity> findByFollowingId(@Param("followingId") String followingId, Pageable pageable);
    
    @Query("SELECT COUNT(uf) FROM UserFollowEntity uf WHERE uf.followerId = :userId")
    long countFollowing(@Param("userId") String userId);
    
    @Query("SELECT COUNT(uf) FROM UserFollowEntity uf WHERE uf.followingId = :userId")
    long countFollowers(@Param("userId") String userId);
}
