package com.vinaacademy.platform.feature.reading;

import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("READING")
public class Reading extends Lesson {
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}
