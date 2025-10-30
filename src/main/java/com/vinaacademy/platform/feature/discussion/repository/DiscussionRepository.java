// DiscussionRepository.java
package com.vinaacademy.platform.feature.discussion.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vinaacademy.platform.feature.discussion.entity.Discussion;

public interface DiscussionRepository extends JpaRepository<Discussion, UUID> {
	// Lấy danh sách Root comment + tổng số reply + tổng số like
	@Query("""
			SELECT d.id,
			       d.comment,
			       d.lesson.id,
			       d.user.id,
			       COUNT(DISTINCT r.id) AS replyCount,
			       COUNT(DISTINCT f.id) AS favoriteCount,
			       d.user.fullName,
			       d.user.avatarUrl,
			       d.createdDate
			FROM Discussion d
			    LEFT JOIN Discussion r ON r.parentComment.id = d.id
			    LEFT JOIN Favorite   f ON f.comment.id     = d.id
			WHERE d.lesson.id = :lessonId
			  AND d.parentComment IS NULL
			GROUP BY d.id, d.comment, d.lesson.id, d.user.id, d.user.fullName, d.user.avatarUrl, d.createdDate
			""")
	Page<Object[]> findRootCommentsWithCounts(@Param("lessonId") UUID lessonId, Pageable pageable);

	// Lấy danh sách reply cho 1 comment + tổng số reply con + tổng số like
	@Query("""
			SELECT d.id,
			       d.comment,
			       d.lesson.id,
			       d.user.id,
			       COUNT(DISTINCT r.id) AS replyCount,
			       COUNT(DISTINCT f.id) AS favoriteCount,
			       d.user.fullName,
			       d.user.avatarUrl,
			       d.createdDate
			FROM Discussion d
			    LEFT JOIN Discussion r ON r.parentComment.id = d.id
			    LEFT JOIN Favorite   f ON f.comment.id     = d.id
			WHERE d.parentComment.id = :parentId
			GROUP BY d.id, d.comment, d.lesson.id, d.user.id, d.user.fullName, d.user.avatarUrl, d.createdDate
			""")
	Page<Object[]> findRepliesWithCounts(@Param("parentId") UUID parentId, Pageable pageable);

}
