package com.leorces.persistence.postgres.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "activity")
public class ActivityEntity implements Persistable<String> {

    @Id
    @Column("activity_id")
    @JsonProperty("id")
    private String id;

    @Column("process_id")
    @JsonProperty("process_id")
    private String processId;

    @Column("activity_definition_id")
    @JsonProperty("activity_definition_id")
    private String activityDefinitionId;

    @Column("activity_parent_definition_id")
    @JsonProperty("parent_activity_definition_id")
    private String parentActivityDefinitionId;

    @Column("process_definition_id")
    @JsonProperty("process_definition_id")
    private String processDefinitionId;

    @Column("process_definition_key")
    @JsonProperty("process_definition_key")
    private String processDefinitionKey;

    @Column("activity_type")
    @JsonProperty("type")
    private String type;

    @Column("activity_state")
    @JsonProperty("state")
    private String state;

    @Column("activity_topic")
    @JsonProperty("topic")
    private String topic;

    @Column("activity_retries")
    @JsonProperty("retries")
    private int retries;

    @Column("activity_timeout")
    @JsonProperty("activity_timeout")
    private LocalDateTime timeout;

    @Column("activity_failure_reason")
    @JsonProperty("activity_failure_reason")
    private String failureReason;

    @Column("activity_failure_trace")
    @JsonProperty("activity_failure_trace")
    private String failureTrace;

    @Column("activity_created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Column("activity_updated_at")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Column("activity_started_at")
    @JsonProperty("started_at")
    private LocalDateTime startedAt;

    @Column("activity_completed_at")
    @JsonProperty("completed_at")
    private LocalDateTime completedAt;

    @Column("activity_async")
    @JsonProperty("async")
    private Boolean async;

    @ReadOnlyProperty
    @JsonProperty("process_business_key")
    private String processBusinessKey;

    @Transient
    @JsonIgnore
    private boolean isNew;

    @ReadOnlyProperty
    @JsonProperty("variablesJson")
    private Object variablesJson;

    @Override
    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }

}