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
@Table(name = "job")
public class JobEntity implements Persistable<String> {

    @Id
    @Column("job_id")
    private String id;

    @Column("job_type")
    private String type;

    @Column("job_state")
    private String state;

    @Column("job_input")
    private PGobject input;

    @Column("job_output")
    private PGobject output;

    @Column("job_failure_reason")
    private String failureReason;

    @Column("job_failure_trace")
    private String failureTrace;

    @Column("job_retries")
    private int retries;

    @Column("job_created_at")
    private LocalDateTime createdAt;

    @Column("job_updated_at")
    private LocalDateTime updatedAt;

    @Column("job_started_at")
    private LocalDateTime startedAt;

    @Column("job_completed_at")
    private LocalDateTime completedAt;

    @Transient
    private boolean isNew;

    @Override
    public boolean isNew() {
        return isNew;
    }

}
