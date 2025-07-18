package com.vinaacademy.platform.feature.reading.repository;

import com.vinaacademy.platform.feature.reading.Reading;
import com.vinaacademy.platform.feature.section.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, UUID> {
    /**
     * Find readings by section ordered by their index
     */
    List<Reading> findBySectionOrderByOrderIndex(Section section);
}
