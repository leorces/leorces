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

@Table(name = "history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryEntity implements Persistable<String> {

    @Id
    @Column("process_id")
    private String processId;

    @Column("root_process_id")
    private String rootProcessId;

    @Column("process_parent_id")
    private String parentProcessId;

    @Column("process_business_key")
    private String businessKey;

    @Column("history_data")
    private byte[] data;

    @Column("process_created_at")
    private LocalDateTime createdAt;

    @Column("process_updated_at")
    private LocalDateTime updatedAt;

    @Column("process_started_at")
    private LocalDateTime startedAt;

    @Column("process_completed_at")
    private LocalDateTime completedAt;

    @Transient
    private boolean isNew;


    @Override
    public String getId() {
        return processId;
    }


    @Override
    public boolean isNew() {
        return isNew;
    }

}