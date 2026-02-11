package com.leorces.persistence.postgres.repository;

import com.leorces.persistence.postgres.entity.ActivityExecutionEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.leorces.persistence.postgres.repository.query.activity.CHANGE_STATE.CHANGE_STATE_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.DELETE_ALL_ACTIVE_BY_DEFINITION_IDS.DELETE_ALL_ACTIVE_BY_DEFINITION_IDS_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.FIND_ALL_ACTIVE_BY_DEFINITION_IDS.FIND_ALL_ACTIVE_BY_DEFINITION_IDS_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.FIND_ALL_ACTIVE_BY_PROCESS_ID.FIND_ALL_ACTIVE_BY_PROCESS_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.FIND_ALL_BY_IDS.FIND_ALL_BY_IDS_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.FIND_ALL_BY_PROCESS_ID.FIND_ALL_BY_PROCESS_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.FIND_ALL_FAILED_BY_PROCESS_ID.FIND_ALL_FAILED_BY_PROCESS_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.FIND_BY_DEFINITION_ID.FIND_BY_DEFINITION_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.FIND_BY_ID.FIND_BY_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.FIND_TIMED_OUT.FIND_TIMED_OUT_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.IS_ALL_COMPLETED_BY_DEFINITION_IDS.IS_ALL_COMPLETED_BY_DEFINITION_IDS_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.IS_ALL_COMPLETED_BY_PROCESS_ID.IS_ALL_COMPLETED_BY_PROCESS_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.IS_ANY_FAILED.IS_ANY_FAILED_QUERY;
import static com.leorces.persistence.postgres.repository.query.activity.POLL.POLL_QUERY;

@Repository
public interface ActivityRepository extends CrudRepository<ActivityExecutionEntity, String> {

    @Query(FIND_BY_ID_QUERY)
    Optional<ActivityExecutionEntity> findById(@Param("activityId") String activityId);

    @Query(FIND_BY_DEFINITION_ID_QUERY)
    List<ActivityExecutionEntity> findByDefinitionId(@Param("processId") String processId,
                                                     @Param("definitionId") String definitionId);

    @Query(POLL_QUERY)
    List<ActivityExecutionEntity> poll(@Param("topic") String topic,
                                       @Param("processDefinitionKey") String processDefinitionKey,
                                       @Param("limit") int limit);

    @Query(FIND_ALL_BY_IDS_QUERY)
    List<ActivityExecutionEntity> findAllByIds(@Param("ids") String[] activityIds);

    @Query(FIND_ALL_BY_PROCESS_ID_QUERY)
    List<ActivityExecutionEntity> findAllByProcessId(@Param("processId") String processId);

    @Query(FIND_ALL_ACTIVE_BY_DEFINITION_IDS_QUERY)
    List<ActivityExecutionEntity> findActive(@Param("processId") String processId,
                                             @Param("definitionIds") String[] definitionIds);

    @Query(FIND_ALL_ACTIVE_BY_PROCESS_ID_QUERY)
    List<ActivityExecutionEntity> findActive(@Param("processId") String processId);

    @Query(FIND_ALL_FAILED_BY_PROCESS_ID_QUERY)
    List<ActivityExecutionEntity> findFailed(@Param("processId") String processId);

    @Query(FIND_TIMED_OUT_QUERY)
    List<ActivityExecutionEntity> findTimedOut(@Param("limit") int limit);

    @Query(IS_ANY_FAILED_QUERY)
    boolean isAnyFailed(@Param("processId") String processId);

    @Query(IS_ALL_COMPLETED_BY_PROCESS_ID_QUERY)
    boolean isAllCompleted(@Param("processId") String processId);

    @Query(IS_ALL_COMPLETED_BY_DEFINITION_IDS_QUERY)
    boolean isAllCompleted(@Param("processId") String processId,
                           @Param("definitionIds") String[] definitionIds);

    @Modifying
    @Query(CHANGE_STATE_QUERY)
    void changeState(@Param("activityId") String activityId,
                     @Param("state") String state);

    @Query(DELETE_ALL_ACTIVE_BY_DEFINITION_IDS_QUERY)
    List<String> deleteAllActive(@Param("processId") String activityId,
                                 @Param("definitionIds") String[] definitionIds);

}
