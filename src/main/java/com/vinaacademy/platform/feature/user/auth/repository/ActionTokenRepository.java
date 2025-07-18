package com.vinaacademy.platform.feature.user.auth.repository;

import com.vinaacademy.platform.feature.user.auth.entity.ActionToken;
import com.vinaacademy.platform.feature.user.auth.enums.ActionTokenType;
import com.vinaacademy.platform.feature.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface ActionTokenRepository extends JpaRepository<ActionToken, Long> {
    Optional<ActionToken> findByTokenAndType(String token, ActionTokenType type);

    Optional<ActionToken> findByUserAndType(User user, ActionTokenType actionTokenType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ActionToken a WHERE a.user = :user AND a.type = :type")
    @QueryHints({ @QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000") })
    Optional<ActionToken> findForUpdate(@Param("user") User user, @Param("type") ActionTokenType type);

}
