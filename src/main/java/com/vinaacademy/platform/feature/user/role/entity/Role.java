package com.vinaacademy.platform.feature.user.role.entity;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_permission", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Builds the collection of Spring Security GrantedAuthority entries for this role.
     *
     * <p>Returns an empty collection if the role's code is blank. If the role has no
     * permissions, returns a single authority named "ROLE_{code}". Otherwise returns
     * an authority for the role ("ROLE_{code}") plus one authority per associated
     * permission code. All authorities are represented as SimpleGrantedAuthority.
     *
     * @return a collection of GrantedAuthority representing the role and its permissions
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (StringUtils.isBlank(this.getCode())) {
            return Set.of();
        }
        if (this.permissions == null || this.permissions.isEmpty()) {
            return Set.of(new SimpleGrantedAuthority("ROLE_" + this.getCode()));
        }
        Stream<String> role = Stream.of("ROLE_" + this.getCode());
        Stream<String> permissions = this.getPermissions().stream().map(Permission::getCode);
        return Stream.concat(role, permissions)
            .map(SimpleGrantedAuthority::new)
            .toList();
    }
}
