// FavoriteRepository.java
package com.vinaacademy.platform.feature.discussion.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vinaacademy.platform.feature.discussion.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    Optional<Favorite> findByUserIdAndCommentId(UUID userId, UUID commentId);
    boolean existsByUserIdAndCommentId(UUID userId, UUID commentId);
    
    @Query("SELECT f.comment.id FROM Favorite f WHERE f.user.id = :userId AND f.comment.id IN :commentIds")
    List<UUID> findLikedCommentIdsByUserAndCommentIds(@Param("userId") UUID userId, @Param("commentIds") List<UUID> commentIds);
}
