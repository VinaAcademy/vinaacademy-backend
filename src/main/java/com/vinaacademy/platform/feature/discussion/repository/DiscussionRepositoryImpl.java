package com.vinaacademy.platform.feature.discussion.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.vinaacademy.platform.feature.discussion.dto.DiscussionSummaryDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class DiscussionRepositoryImpl implements DiscussionRepositoryCustom {
	
    private final EntityManager entityManager;

    public DiscussionRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<DiscussionSummaryDto> findRootCommentSummariesWithPriority(UUID lessonId, UUID currentUserId, Pageable pageable) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT new com.vinaacademy.platform.feature.discussion.dto.DiscussionSummaryDto(")
            .append(" d.id, d.comment, d.lesson.id, d.user.id, ")
            .append(" (SELECT COUNT(r2.id) FROM Discussion r2 WHERE r2.parentComment.id = d.id), ")
            .append(" (SELECT COUNT(f2.id) FROM Favorite f2 WHERE f2.comment.id = d.id), ")
            .append(" d.user.fullName, d.user.avatarUrl, d.createdDate, ")
            .append(" CASE WHEN ((SELECT COUNT(f3.id) FROM Favorite f3 WHERE f3.user.id = :currentUserId AND f3.comment.id = d.id) > 0) THEN true ELSE false END")
            .append(" ) FROM Discussion d ")
            .append(" WHERE d.lesson.id = :lessonId AND d.parentComment IS NULL ")
            .append(" AND NOT EXISTS (")
            .append("   SELECT 1 FROM DiscussionModerationFlag dmf ")
            .append("   WHERE dmf.discussion.id = d.id ")
            .append("   AND dmf.status IN ('PENDING', 'REJECTED') ")
            .append("   AND dmf.flagType IN ('TOXIC', 'EXTREME_NEGATIVE', 'SPAM')")
            .append(" )");

        String orderBy = buildOrderBy(currentUserId, pageable);
        String queryString = jpql.toString() + orderBy;

        Query query = entityManager.createQuery(queryString);
        query.setParameter("lessonId", lessonId);
        query.setParameter("currentUserId", currentUserId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<DiscussionSummaryDto> content = query.getResultList();

        long total = countRootComments(lessonId);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<DiscussionSummaryDto> findReplySummariesWithPriority(UUID parentId, UUID currentUserId, Pageable pageable) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT new com.vinaacademy.platform.feature.discussion.dto.DiscussionSummaryDto(")
            .append(" d.id, d.comment, d.lesson.id, d.user.id, ")
            .append(" (SELECT COUNT(r2.id) FROM Discussion r2 WHERE r2.parentComment.id = d.id), ")
            .append(" (SELECT COUNT(f2.id) FROM Favorite f2 WHERE f2.comment.id = d.id), ")
            .append(" d.user.fullName, d.user.avatarUrl, d.createdDate, ")
            .append(" CASE WHEN ((SELECT COUNT(f3.id) FROM Favorite f3 WHERE f3.user.id = :currentUserId AND f3.comment.id = d.id) > 0) THEN true ELSE false END")
            .append(" ) FROM Discussion d ")
            .append(" WHERE d.parentComment.id = :parentId ")
            .append(" AND NOT EXISTS (")
            .append("   SELECT 1 FROM DiscussionModerationFlag dmf ")
            .append("   WHERE dmf.discussion.id = d.id ")
            .append("   AND dmf.status IN ('PENDING', 'REJECTED') ")
            .append("   AND dmf.flagType IN ('TOXIC', 'EXTREME_NEGATIVE', 'SPAM')")
            .append(" )");

        String orderBy = buildOrderBy(currentUserId, pageable);
        String queryString = jpql.toString() + orderBy;

        Query query = entityManager.createQuery(queryString);
        query.setParameter("parentId", parentId);
        query.setParameter("currentUserId", currentUserId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<DiscussionSummaryDto> content = query.getResultList();

        long total = countReplies(parentId);
        return new PageImpl<>(content, pageable, total);
    }

    private String buildOrderBy(UUID currentUserId, Pageable pageable) {
        StringBuilder order = new StringBuilder();
        order.append(" ORDER BY CASE WHEN d.user.id = :currentUserId THEN 0 ELSE 1 END");
        List<String> sortClauses = new ArrayList<>();
        if (pageable != null && pageable.getSort() != null) {
            for (Sort.Order s : pageable.getSort()) {
                String property = mapSortableProperty(s.getProperty());
                if (property != null) {
                    sortClauses.add(" " + property + " " + (s.isAscending() ? "ASC" : "DESC"));
                }
            }
        }
        if (!sortClauses.isEmpty()) {
            order.append(",");
            order.append(String.join(",", sortClauses));
        } else {
            order.append(", d.createdDate DESC");
        }
        return order.toString();
    }

    private String mapSortableProperty(String key) {
        if (key == null) return null;
        switch (key) {
            case "createdDate":
                return "d.createdDate";
            case "replyCount":
                return "(SELECT COUNT(r2.id) FROM Discussion r2 WHERE r2.parentComment.id = d.id)";
            case "favoriteCount":
                return "(SELECT COUNT(f2.id) FROM Favorite f2 WHERE f2.comment.id = d.id)";
            case "userFullName":
                return "d.user.fullName";
            default:
                return null;
        }
    }

    private long countRootComments(UUID lessonId) {
        String jpql = "SELECT COUNT(d.id) FROM Discussion d " +
                      "WHERE d.lesson.id = :lessonId AND d.parentComment IS NULL " +
                      "AND NOT EXISTS (" +
                      "  SELECT 1 FROM DiscussionModerationFlag dmf " +
                      "  WHERE dmf.discussion.id = d.id " +
                      "  AND dmf.status IN ('PENDING', 'REJECTED') " +
                  "  AND dmf.flagType IN ('TOXIC', 'EXTREME_NEGATIVE', 'SPAM')" +
                      ")";
        Query countQuery = entityManager.createQuery(jpql);
        countQuery.setParameter("lessonId", lessonId);
        return (Long) countQuery.getSingleResult();
    }

    private long countReplies(UUID parentId) {
        String jpql = "SELECT COUNT(d.id) FROM Discussion d " +
                      "WHERE d.parentComment.id = :parentId " +
                      "AND NOT EXISTS (" +
                      "  SELECT 1 FROM DiscussionModerationFlag dmf " +
                      "  WHERE dmf.discussion.id = d.id " +
                      "  AND dmf.status IN ('PENDING', 'REJECTED') " +
                  "  AND dmf.flagType IN ('TOXIC', 'EXTREME_NEGATIVE', 'SPAM')" +
                      ")";
        Query countQuery = entityManager.createQuery(jpql);
        countQuery.setParameter("parentId", parentId);
        return (Long) countQuery.getSingleResult();
    }
}
