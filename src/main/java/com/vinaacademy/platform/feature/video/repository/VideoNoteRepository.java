package com.vinaacademy.platform.feature.video.repository;

import com.vinaacademy.platform.feature.video.entity.VideoNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoNoteRepository extends JpaRepository<VideoNote, Long> {
    List<VideoNote> findByVideoIdAndUserId(UUID videoId,UUID userId);

    List<VideoNote> findByUserId(UUID userId);

    Optional<VideoNote> findByIdAndUserId(Long id, UUID userId);

    void deleteByIdAndUserId(Long id, UUID userId);
}
