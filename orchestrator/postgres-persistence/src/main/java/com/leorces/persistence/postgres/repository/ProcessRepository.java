package com.leorces.persistence.postgres.repository;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.postgres.entity.ProcessEntity;
import com.leorces.persistence.postgres.entity.ProcessExecutionEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.leorces.persistence.postgres.repository.query.process.BATCH_UPDATE_DEFINITION_ID.BATCH_UPDATE_DEFINITION_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.CHANGE_STATE.CHANGE_STATE_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.COMPLETE.COMPLETE_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.COUNT_ALL_WITH_FILTERS.COUNT_ALL_WITH_FILTERS_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.DELETE.DELETE_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.FIND_ALL_BY_FILTERS.FIND_ALL_BY_FILTERS_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.FIND_ALL_FULLY_COMPLETED_FOR_UPDATE.FIND_ALL_FULLY_COMPLETED_FOR_UPDATE_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.FIND_ALL_WITH_PAGINATION.FIND_ALL_WITH_PAGINATION_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.FIND_BY_ID.FIND_BY_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.FIND_EXECUTIONS_FOR_UPDATE.FIND_EXECUTIONS_FOR_UPDATE_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.FIND_EXECUTION_BY_ID.FIND_EXECUTION_BY_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.INCIDENT.INCIDENT_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.RESUME_BY_DEFINITION_ID.RESUME_BY_DEFINITION_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.RESUME_BY_DEFINITION_KEY.RESUME_BY_DEFINITION_KEY_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.RESUME_BY_ID.RESUME_BY_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.RUN.RUN_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.SUSPEND_BY_DEFINITION_ID.SUSPEND_BY_DEFINITION_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.SUSPEND_BY_DEFINITION_KEY.SUSPEND_BY_DEFINITION_KEY_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.SUSPEND_BY_ID.SUSPEND_BY_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.TERMINATE.TERMINATE_QUERY;
import static com.leorces.persistence.postgres.repository.query.process.UPDATE_DEFINITION_ID.UPDATE_DEFINITION_ID_QUERY;

@Repository
public interface ProcessRepository extends CrudRepository<ProcessEntity, String> {

    @Query(FIND_BY_ID_QUERY)
    Optional<ProcessEntity> findById(@Param("processId") String processId);

    @Query(FIND_ALL_WITH_PAGINATION_QUERY)
    List<ProcessEntity> findAllWithPagination(@Param("offset") long offset,
                                              @Param("limit") int limit,
                                              @Param("order") String order,
                                              @Param("sort_by_field") String sortByField,
                                              @Param("filter") String filter,
                                              @Param("state") String state);

    @Query(FIND_ALL_BY_FILTERS_QUERY)
    List<ProcessEntity> findAll(@Param("processId") String processId,
                                @Param("processDefinitionKey") String processDefinitionKey,
                                @Param("processDefinitionId") String processDefinitionId,
                                @Param("businessKey") String businessKey,
                                @Param("variableKeys") String[] variableKeys,
                                @Param("variableValues") String[] variableValues,
                                @Param("variableCount") int variableCount);

    @Query(FIND_EXECUTION_BY_ID_QUERY)
    Optional<ProcessExecutionEntity> findExecutionById(@Param("processId") String processId);

    @Query(FIND_EXECUTIONS_FOR_UPDATE_QUERY)
    List<ProcessExecutionEntity> findExecutionsForUpdate(@Param("definitionId") String definitionId,
                                                         @Param("limit") int limit);

    @Query(FIND_ALL_FULLY_COMPLETED_FOR_UPDATE_QUERY)
    List<ProcessExecutionEntity> findAllFullyCompletedForUpdate(@Param("limit") int limit);

    @Query(COUNT_ALL_WITH_FILTERS_QUERY)
    long countAllWithFilters(@Param("filter") String filter, @Param("state") String state);

    @Modifying
    @Query(CHANGE_STATE_QUERY)
    void changeState(@Param("processId") String processId, @Param("state") String state);

    @Query(RUN_QUERY)
    ProcessEntity run(@Param("processId") String processId,
                      @Param("rootProcessId") String rootProcessId,
                      @Param("parentProcessId") String parentProcessId,
                      @Param("definitionId") String definitionId,
                      @Param("definitionKey") String definitionKey,
                      @Param("businessKey") String businessKey,
                      @Param("suspended") boolean suspended);

    @Modifying
    @Query(COMPLETE_QUERY)
    void complete(@Param("processId") String processId);

    @Modifying
    @Query(TERMINATE_QUERY)
    void terminate(@Param("processId") String processId);

    @Modifying
    @Query(DELETE_QUERY)
    void delete(@Param("processId") String processId);

    @Modifying
    @Query(INCIDENT_QUERY)
    void incident(@Param("processId") String processId);

    @Modifying
    @Query(SUSPEND_BY_ID_QUERY)
    void suspendById(@Param("processId") String processId);

    @Modifying
    @Query(SUSPEND_BY_DEFINITION_ID_QUERY)
    void suspendByDefinitionId(@Param("definitionId") String definitionId);

    @Modifying
    @Query(SUSPEND_BY_DEFINITION_KEY_QUERY)
    void suspendByDefinitionKey(@Param("definitionKey") String definitionKey);

    @Modifying
    @Query(RESUME_BY_ID_QUERY)
    void resumeById(@Param("processId") String processId);

    @Modifying
    @Query(RESUME_BY_DEFINITION_ID_QUERY)
    void resumeByDefinitionId(@Param("definitionId") String definitionId);

    @Modifying
    @Query(RESUME_BY_DEFINITION_KEY_QUERY)
    void resumeByDefinitionKey(@Param("definitionKey") String definitionKey);

    @Query(UPDATE_DEFINITION_ID_QUERY)
    List<String> updateDefinitionId(@Param("toDefinitionId") String toDefinitionId,
                                    @Param("processIds") String[] processIds);

    @Query(BATCH_UPDATE_DEFINITION_ID_QUERY)
    List<String> updateDefinitionId(@Param("fromDefinitionId") String fromDefinitionId,
                                    @Param("toDefinitionId") String toDefinitionId,
                                    @Param("limit") int limit);

    default PageableData<ProcessEntity> findAll(Pageable pageable) {
        var orderName = pageable.order() != null ? pageable.order().name() : Pageable.Direction.DESC.name();
        var data = findAllWithPagination(
                pageable.offset(),
                pageable.limit(),
                orderName,
                pageable.sortByField(),
                pageable.filter(),
                pageable.state()
        );
        var total = countAllWithFilters(pageable.filter(), pageable.state());

        return new PageableData<>(data, total);
    }

}
