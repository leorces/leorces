package com.leorces.persistence.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shedlock")
public class ShedlockEntity {

    @Id
    @Column("name")
    private String name;

    @Column("lock_until")
    private LocalDateTime lockUntil;

    @Column("locked_at")
    private LocalDateTime lockedAt;

    @Column("locked_by")
    private String lockedBy;

}