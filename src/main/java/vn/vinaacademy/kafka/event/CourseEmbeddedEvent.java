package vn.vinaacademy.kafka.event;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEmbeddedEvent {
	private UUID id;
	private String title;
	private String description;
	private String instructorName;
	private String categoryName;
	private BigDecimal price;
}