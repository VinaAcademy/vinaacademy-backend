package com.vinaacademy.platform.feature.notification.entity;

import com.vinaacademy.platform.feature.common.entity.SoftDeleteEntity;
import com.vinaacademy.platform.feature.notification.enums.NotificationType;
import com.vinaacademy.platform.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification extends SoftDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "content")
	private String content;
	
	@Column(name = "is_read", nullable = false)
	private Boolean isRead;
	
	@Column(name = "read_at", nullable = true)
	private LocalDateTime readAt;
	
	@ManyToOne
    @JoinColumn(name = "recipient")
	private User user;
	
	@Column(name = "target_url")
	private String targetUrl;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "type")
    @Enumerated(EnumType.STRING)
    private NotificationType type = NotificationType.SYSTEM;

}	
