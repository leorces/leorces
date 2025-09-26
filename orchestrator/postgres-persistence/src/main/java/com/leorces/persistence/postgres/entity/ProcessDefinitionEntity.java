package com.leorces.persistence.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "definition")
public class ProcessDefinitionEntity implements Persistable<String> {

    @Id
    @Column("definition_id")
    private String id;

    @Column("definition_key")
    private String key;

    @Column("definition_name")
    private String name;

    @Column("definition_version")
    private Integer version;

    @Column("definition_schema")
    private String schema;

    @Column("definition_origin")
    private String origin;

    @Column("definition_deployment")
    private String deployment;

    @Column("definition_created_at")
    private LocalDateTime createdAt;

    @Column("definition_updated_at")
    private LocalDateTime updatedAt;

    @Column("definition_data")
    private PGobject data;

    @Transient
    private boolean isNew;

    @Override
    public boolean isNew() {
        return isNew;
    }

}