package com.leorces.persistence.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "activity")
public class ActivityExecutionEntity implements Persistable<String> {

    @Id
    @Column("activity_id")
    private String id;

    @Column("process_id")
    private String processId;

    @Column("activity_definition_id")
    private String activityDefinitionId;

    @Column("activity_parent_definition_id")
    private String parentActivityDefinitionId;

    @Column("process_definition_key")
    private String processDefinitionKey;

    @Column("activity_type")
    private String type;

    @Column("activity_state")
    private String state;

    @Column("activity_topic")
    private String topic;

    @Column("activity_retries")
    private int retries;

    @Column("activity_timeout")
    private LocalDateTime timeout;

    @Column("activity_failure_reason")
    private String failureReason;

    @Column("activity_failure_trace")
    private String failureTrace;

    @Column("activity_created_at")
    private LocalDateTime createdAt;

    @Column("activity_updated_at")
    private LocalDateTime updatedAt;

    @Column("activity_started_at")
    private LocalDateTime startedAt;

    @Column("activity_completed_at")
    private LocalDateTime completedAt;

    @Column("activity_async")
    private Boolean async;

    @Transient
    private boolean isNew;

    @ReadOnlyProperty
    private PGobject variablesJson;

    // Process
    @ReadOnlyProperty
    private String rootProcessId;

    @ReadOnlyProperty
    private String processParentId;

    @ReadOnlyProperty
    private String processBusinessKey;

    @ReadOnlyProperty
    private String processState;

    @ReadOnlyProperty
    private boolean processSuspended;

    @ReadOnlyProperty
    private LocalDateTime processCreatedAt;

    @ReadOnlyProperty
    private LocalDateTime processUpdatedAt;

    @ReadOnlyProperty
    private LocalDateTime processStartedAt;

    @ReadOnlyProperty
    private LocalDateTime processCompletedAt;

    // Process definition
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
    private boolean definitionSuspended;

    @ReadOnlyProperty
    private PGobject definitionData;

    @Override
    public boolean isNew() {
        return isNew;
    }

}