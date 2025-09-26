package com.leorces.persistence.postgres.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Table(name = "variable")
public class VariableEntity implements Persistable<String> {

    @Id
    @Column("variable_id")
    @JsonProperty("id")
    private String id;

    @Column("process_id")
    @JsonProperty("process_id")
    private String processId;

    @Column("execution_id")
    @JsonProperty("execution_id")
    private String executionId;

    @Column("execution_definition_id")
    @JsonProperty("execution_definition_id")
    private String executionDefinitionId;

    @Column("variable_key")
    @JsonProperty("var_key")
    private String varKey;

    @Column("variable_value")
    @JsonProperty("var_value")
    private String varValue;

    @Column("variable_type")
    @JsonProperty("type")
    private String type;

    @Column("variable_created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Column("variable_updated_at")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Transient
    @JsonIgnore
    private boolean isNew;

    @Override
    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }

}
