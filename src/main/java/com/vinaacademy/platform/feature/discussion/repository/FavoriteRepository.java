// FavoriteRepository.java
package com.vinaacademy.platform.feature.discussion.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.vinaacademy.platform.feature.discussion.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    Optional<Favorite> findByUserIdAndCommentId(UUID userId, UUID commentId);
    boolean existsByUserIdAndCommentId(UUID userId, UUID commentId);
    
    @Query("SELECT f.comment.id FROM Favorite f WHERE f.user.id = :userId AND f.comment.id IN :commentIds")
    List<UUID> findLikedCommentIdsByUserAndCommentIds(@Param("userId") UUID userId, @Param("commentIds") List<UUID> commentIds);

    @Transactional
    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId AND f.comment.id = :commentId")
    int deleteByUserIdAndCommentId(@Param("userId") UUID userId, @Param("commentId") UUID commentId);
}
