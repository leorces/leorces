package com.leorces.persistence.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "process")
public class ProcessExecutionEntity {

    @Id
    @Column("process_id")
    private String id;

    @Column("root_process_id")
    private String rootProcessId;

    @Column("process_parent_id")
    private String parentProcessId;

    @Column("process_definition_id")
    private String processDefinitionId;

    @Column("process_definition_key")
    private String processDefinitionKey;

    @Column("process_business_key")
    private String businessKey;

    @Column("process_state")
    private String state;

    private ProcessDefinitionEntity definition;

    @Column("process_created_at")
    private LocalDateTime createdAt;

    @Column("process_updated_at")
    private LocalDateTime updatedAt;

    @Column("process_started_at")
    private LocalDateTime startedAt;

    @Column("process_completed_at")
    private LocalDateTime completedAt;

    @ReadOnlyProperty
    private PGobject variablesJson;

    @ReadOnlyProperty
    private PGobject activitiesJson;

    @ReadOnlyProperty
    private String definitionId;

    @ReadOnlyProperty
    private String definitionKey;

    @ReadOnlyProperty
    private String definitionName;

    @ReadOnlyProperty
    private Integer definitionVersion;

    @ReadOnlyProperty
    private String definitionSchema;

    @ReadOnlyProperty
    private String definitionOrigin;

    @ReadOnlyProperty
    private String definitionDeployment;

    @ReadOnlyProperty
    private LocalDateTime definitionCreatedAt;

    @ReadOnlyProperty
    private LocalDateTime definitionUpdatedAt;

    @ReadOnlyProperty
    private PGobject definitionData;

}
