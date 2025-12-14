// DiscussionRepository.java
package com.vinaacademy.platform.feature.discussion.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vinaacademy.platform.feature.discussion.entity.Discussion;

public interface DiscussionRepository extends JpaRepository<Discussion, UUID>, DiscussionRepositoryCustom {
	// Lấy danh sách Root comment tối ưu: chỉ chọn đúng cột cần thiết cho DTO
	@Query("""
			SELECT 
				d.id                 AS id,
				d.comment            AS comment,
				d.lesson.id          AS lessonId,
				d.user.id            AS userId,
				(SELECT COUNT(r2.id) FROM Discussion r2 WHERE r2.parentComment.id = d.id) AS replyCount,
				(SELECT COUNT(f2.id) FROM Favorite   f2 WHERE f2.comment.id        = d.id) AS favoriteCount,
				d.user.fullName      AS userFullName,
				d.user.avatarUrl     AS avatarUrl,
				d.createdDate        AS createdDate,
				CASE WHEN (
					(SELECT COUNT(f3.id) FROM Favorite f3 
					 WHERE f3.user.id = :currentUserId AND f3.comment.id = d.id) > 0
				) THEN true ELSE false END AS likedByCurrentUser
			FROM Discussion d
			WHERE d.lesson.id = :lessonId AND d.parentComment IS NULL
			ORDER BY CASE WHEN d.user.id = :currentUserId THEN 0 ELSE 1 END, d.createdDate DESC
			""")
	Page<com.vinaacademy.platform.feature.discussion.repository.projection.DiscussionSummary> findRootCommentSummaries(
		@Param("lessonId") UUID lessonId,
		@Param("currentUserId") UUID currentUserId,
		Pageable pageable
	);

	// Lấy danh sách reply tối ưu: chỉ chọn đúng cột cần thiết cho DTO
	@Query("""
			SELECT 
				d.id                 AS id,
				d.comment            AS comment,
				d.lesson.id          AS lessonId,
				d.user.id            AS userId,
				(SELECT COUNT(r2.id) FROM Discussion r2 WHERE r2.parentComment.id = d.id) AS replyCount,
				(SELECT COUNT(f2.id) FROM Favorite   f2 WHERE f2.comment.id        = d.id) AS favoriteCount,
				d.user.fullName      AS userFullName,
				d.user.avatarUrl     AS avatarUrl,
				d.createdDate        AS createdDate,
				CASE WHEN (
					(SELECT COUNT(f3.id) FROM Favorite f3 
					 WHERE f3.user.id = :currentUserId AND f3.comment.id = d.id) > 0
				) THEN true ELSE false END AS likedByCurrentUser
			FROM Discussion d
			WHERE d.parentComment.id = :parentId
			ORDER BY CASE WHEN d.user.id = :currentUserId THEN 0 ELSE 1 END, d.createdDate DESC
			""")
	Page<com.vinaacademy.platform.feature.discussion.repository.projection.DiscussionSummary> findReplySummaries(
		@Param("parentId") UUID parentId,
		@Param("currentUserId") UUID currentUserId,
		Pageable pageable
	);

}
