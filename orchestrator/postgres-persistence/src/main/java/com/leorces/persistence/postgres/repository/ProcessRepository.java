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

import static com.leorces.persistence.postgres.repository.query.ProcessQueries.*;

@Repository
public interface ProcessRepository extends CrudRepository<ProcessEntity, String> {

    @Query(FIND_BY_ID)
    Optional<ProcessEntity> findById(@Param("processId") String processId);

    @Query(FIND_ALL_WITH_PAGINATION)
    List<ProcessEntity> findAllWithPagination(@Param("offset") long offset,
                                              @Param("limit") int limit,
                                              @Param("order") String order,
                                              @Param("sort_by_field") String sortByField,
                                              @Param("filter") String filter,
                                              @Param("state") String state);

    @Query(FIND_ALL_BY_FILTERS)
    List<ProcessEntity> findAll(@Param("processId") String processId,
                                @Param("processDefinitionKey") String processDefinitionKey,
                                @Param("processDefinitionId") String processDefinitionId,
                                @Param("businessKey") String businessKey,
                                @Param("variableKeys") String[] variableKeys,
                                @Param("variableValues") String[] variableValues,
                                @Param("variableCount") int variableCount);

    @Query(FIND_BY_ID_WITH_ACTIVITIES)
    Optional<ProcessExecutionEntity> findByIdWithActivities(@Param("processId") String processId);

    @Query(FIND_ALL_FULLY_COMPLETED)
    List<ProcessExecutionEntity> findAllFullyCompleted(@Param("limit") int limit);

    @Query(COUNT_ALL_WITH_FILTERS)
    long countAllWithFilters(@Param("filter") String filter, @Param("state") String state);

    @Modifying
    @Query(CHANGE_STATE)
    void changeState(@Param("processId") String processId, @Param("state") String state);

    @Query(RUN)
    ProcessEntity run(@Param("processId") String processId,
                      @Param("rootProcessId") String rootProcessId,
                      @Param("parentProcessId") String parentProcessId,
                      @Param("definitionId") String definitionId,
                      @Param("definitionKey") String definitionKey,
                      @Param("businessKey") String businessKey,
                      @Param("suspended") boolean suspended);

    @Modifying
    @Query(COMPLETE)
    void complete(@Param("processId") String processId);

    @Modifying
    @Query(TERMINATE)
    void terminate(@Param("processId") String processId);

    @Modifying
    @Query(INCIDENT)
    void incident(@Param("processId") String processId);

    @Modifying
    @Query(SUSPEND_BY_ID)
    void suspendById(@Param("processId") String processId);

    @Modifying
    @Query(SUSPEND_BY_DEFINITION_ID)
    void suspendByDefinitionId(@Param("definitionId") String definitionId);

    @Modifying
    @Query(SUSPEND_BY_DEFINITION_KEY)
    void suspendByDefinitionKey(@Param("definitionKey") String definitionKey);

    @Modifying
    @Query(RESUME_BY_ID)
    void resumeById(@Param("processId") String processId);

    @Modifying
    @Query(RESUME_BY_DEFINITION_ID)
    void resumeByDefinitionId(@Param("definitionId") String definitionId);

    @Modifying
    @Query(RESUME_BY_DEFINITION_KEY)
    void resumeByDefinitionKey(@Param("definitionKey") String definitionKey);

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
