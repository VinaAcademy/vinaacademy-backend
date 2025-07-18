package com.vinaacademy.platform.feature.email;

import com.vinaacademy.platform.feature.email.entity.EmailAccountUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

public interface EmailAccountUsageRepository extends JpaRepository<EmailAccountUsage, Long> {
    Optional<EmailAccountUsage> findByUsername(String username);

    @Modifying
    @Transactional
    @Query("UPDATE EmailAccountUsage e SET e.emailCount = 0, e.lastResetDate = :today WHERE e.lastResetDate <> :today OR e.lastResetDate IS NULL")
    void resetEmailCountsIfNotUpdatedToday(LocalDate today);
}
