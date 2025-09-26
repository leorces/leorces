package com.leorces.persistence.postgres.repository;

import com.leorces.persistence.postgres.entity.ActivityQueueEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.leorces.persistence.postgres.repository.query.ActivityQueueQueries.FIND_ALL;

@Repository
public interface ActivityQueueRepository extends CrudRepository<ActivityQueueEntity, String> {

    @Query(FIND_ALL)
    List<String> findAll(@Param("topic") String topic,
                         @Param("processDefinitionKey") String processDefinitionKey,
                         @Param("limit") int limit);

}
