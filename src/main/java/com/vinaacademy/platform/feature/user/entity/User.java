package com.vinaacademy.platform.feature.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vinaacademy.platform.feature.cart.entity.Cart;
import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import com.vinaacademy.platform.feature.review.entity.CourseReview;
import com.vinaacademy.platform.feature.user.role.entity.Role;
import com.vinaacademy.platform.feature.video.entity.VideoNote;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = "email"),
      @UniqueConstraint(columnNames = "username")
    })
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class User extends BaseEntity implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "username", nullable = false, unique = true)
  private String username;

  @JsonIgnore
  @Column(name = "password")
  private String password;

  @Column(name = "phone", unique = true)
  private String phone;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "is_collaborator")
  private boolean isCollaborator;

  @Column(name = "birthday")
  private LocalDate birthday;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @BatchSize(size = 10)
  private Set<Role> roles;

  @Column(name = "is_enabled")
  private boolean enabled;

  @Column(name = "is_Using_2FA")
  private boolean isUsing2FA = false;

  @Column(name = "failed_attempts")
  @ColumnDefault("0")
  private int failedAttempts;

  @Column(name = "lock_time")
  private LocalDateTime lockTime;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @BatchSize(size = 20)
  private List<VideoNote> videoNotes = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @BatchSize(size = 20)
  private List<Enrollment> enrollments = new ArrayList<>();

  @OneToMany(
      mappedBy = "instructor",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @BatchSize(size = 20)
  private List<CourseInstructor> coursesTaught = new ArrayList<>();

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Cart cart;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @BatchSize(size = 20)
  private List<CourseReview> courseReviews = new ArrayList<>();

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @BatchSize(size = 20)
  private List<UserProgress> progressList;

  @Override
  public String toString() {
    return "User{" + "id=" + id + '}';
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }
    return roles.stream().map(Role::getAuthorities).flatMap(Collection::stream).toList();
  }

  @Override
  public boolean isAccountNonExpired() {
    return UserDetails.super.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return enabled && (lockTime == null || lockTime.isBefore(LocalDateTime.now()));
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return UserDetails.super.isCredentialsNonExpired();
  }
}
