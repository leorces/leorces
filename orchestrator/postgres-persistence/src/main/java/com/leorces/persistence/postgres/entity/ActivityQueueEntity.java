package com.leorces.persistence.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "activity_queue")
public class ActivityQueueEntity implements Persistable<String> {

    @Column("activity_queue_topic")
    private String topic;

    @Id
    @Column("activity_id")
    private String activityId;

    @Column("process_definition_key")
    private String processDefinitionKey;

    @Column("activity_queue_created_at")
    private LocalDateTime createdAt;

    @Column("activity_queue_updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private boolean isNew;

    @Override
    public String getId() {
        return activityId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

}