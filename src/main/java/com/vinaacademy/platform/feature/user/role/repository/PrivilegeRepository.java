package com.vinaacademy.platform.feature.user.role.repository;

import com.vinaacademy.platform.feature.user.role.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeRepository extends JpaRepository<Permission, Long> {
    Permission findByName(String name);
}
