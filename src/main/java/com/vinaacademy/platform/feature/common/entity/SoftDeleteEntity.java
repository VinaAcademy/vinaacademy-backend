package com.vinaacademy.platform.feature.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class SoftDeleteEntity extends BaseEntity{

    @Column(name = "deleted", nullable = false)
    protected boolean deleted;
    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;


    public void softDel() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deleted;
    }
}
