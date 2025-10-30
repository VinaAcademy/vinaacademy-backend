// FavoriteRepository.java
package com.vinaacademy.platform.feature.discussion.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vinaacademy.platform.feature.discussion.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    Optional<Favorite> findByUserIdAndCommentId(UUID userId, UUID commentId);
    boolean existsByUserIdAndCommentId(UUID userId, UUID commentId);
//    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.comment.id = :commentId")
//    long countByCommentId(@Param("commentId") UUID commentId);
}
