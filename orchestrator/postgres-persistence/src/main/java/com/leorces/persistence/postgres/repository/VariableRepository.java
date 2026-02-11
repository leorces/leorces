package com.leorces.persistence.postgres.repository;

import com.leorces.persistence.postgres.entity.VariableEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.leorces.persistence.postgres.repository.query.variable.DELETE_BY_EXECUTION_ID.DELETE_BY_PROCESS_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.variable.DELETE_BY_EXECUTION_IDS.DELETE_BY_PROCESS_IDS_QUERY;
import static com.leorces.persistence.postgres.repository.query.variable.FIND_ALL_PROCESS_VARIABLES.FIND_ALL_PROCESS_VARIABLES_QUERY;
import static com.leorces.persistence.postgres.repository.query.variable.FIND_ALL_VARIABLES_WITHIN_SCOPE.FIND_ALL_VARIABLES_WITHIN_SCOPE_QUERY;
import static com.leorces.persistence.postgres.repository.query.variable.UPDATE_DEFINITION_ID.UPDATE_DEFINITION_ID_QUERY;

@Repository
public interface VariableRepository extends CrudRepository<VariableEntity, String> {

    @Query(FIND_ALL_VARIABLES_WITHIN_SCOPE_QUERY)
    List<VariableEntity> findInScope(@Param("processId") String processId,
                                     @Param("scope") String[] scope);

    @Query(FIND_ALL_PROCESS_VARIABLES_QUERY)
    List<VariableEntity> findInProcess(@Param("processId") String processId);

    @Modifying
    @Query(UPDATE_DEFINITION_ID_QUERY)
    void updateDefinitionId(@Param("toDefinitionId") String toDefinitionId,
                            @Param("processIds") String[] processIds);

    @Modifying
    @Query(DELETE_BY_PROCESS_ID_QUERY)
    void deleteByExecutionId(@Param("executionId") String executionId);

    @Modifying
    @Query(DELETE_BY_PROCESS_IDS_QUERY)
    void deleteByExecutionIds(@Param("executionIds") String[] executionIds);

}
