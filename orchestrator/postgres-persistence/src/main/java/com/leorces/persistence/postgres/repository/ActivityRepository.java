package com.leorces.persistence.postgres.repository;

import com.leorces.persistence.postgres.entity.ActivityExecutionEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.leorces.persistence.postgres.repository.query.ActivityQueries.*;

@Repository
public interface ActivityRepository extends CrudRepository<ActivityExecutionEntity, String> {

    @Query(FIND_BY_ID)
    Optional<ActivityExecutionEntity> findById(@Param("activityId") String activityId);

    @Query(FIND_BY_DEFINITION_ID)
    Optional<ActivityExecutionEntity> findByDefinitionId(@Param("processId") String processId,
                                                         @Param("definitionId") String definitionId);

    @Query(FIND_ALL_ACTIVE_BY_DEFINITION_IDS)
    List<ActivityExecutionEntity> findActive(@Param("processId") String processId,
                                             @Param("definitionIds") List<String> definitionIds);

    @Query(FIND_ALL_ACTIVE_BY_PROCESS_ID)
    List<ActivityExecutionEntity> findActive(@Param("processId") String processId);

    @Query(FIND_ALL_FAILED_BY_PROCESS_ID)
    List<ActivityExecutionEntity> findFailed(@Param("processId") String processId);

    @Query(FIND_TIMED_OUT)
    List<ActivityExecutionEntity> findTimedOut(@Param("limit") int limit);

    @Query(FIND_ALL_BY_IDS)
    List<ActivityExecutionEntity> findAllByIds(@Param("activityIds") List<String> activityIds);

    @Query(IS_ANY_FAILED)
    boolean isAnyFailed(@Param("processId") String processId);

    @Query(IS_ALL_COMPLETED_BY_DEFINITION_ID)
    boolean isAllCompleted(@Param("processId") String processId,
                           @Param("definitionId") String definitionId);

    @Query(IS_ALL_COMPLETED_BY_PROCESS_ID)
    boolean isAllCompleted(@Param("processId") String processId);

    @Query(IS_ALL_COMPLETED)
    boolean isAllCompleted(@Param("processId") String processId,
                           @Param("definitionIds") List<String> definitionIds);

    @Modifying
    @Query(UPDATE_STATUS_BATCH)
    int updateStatusBatch(@Param("activityIds") List<String> activityIds,
                          @Param("newState") String newState);

    @Modifying
    @Query(CHANGE_STATE)
    void changeState(@Param("activityId") String activityId,
                     @Param("state") String state);

    @Modifying
    @Query(DELETE_ALL_ACTIVE_BY_DEFINITION_IDS)
    void deleteAllActive(@Param("processId") String activityId,
                         @Param("definitionIds") List<String> definitionIds);

}
