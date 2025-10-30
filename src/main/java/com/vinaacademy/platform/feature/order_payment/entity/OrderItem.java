package com.vinaacademy.platform.feature.order_payment.entity;

import com.vinaacademy.platform.feature.course.entity.Course;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "price", nullable = false)
  private BigDecimal price;
}
