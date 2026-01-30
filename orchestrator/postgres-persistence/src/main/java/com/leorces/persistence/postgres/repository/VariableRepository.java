package com.leorces.persistence.postgres.repository;

import com.leorces.persistence.postgres.entity.VariableEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.leorces.persistence.postgres.repository.query.variable.FIND_ALL_PROCESS_VARIABLES.FIND_ALL_PROCESS_VARIABLES_QUERY;
import static com.leorces.persistence.postgres.repository.query.variable.FIND_ALL_VARIABLES_WITHIN_SCOPE.FIND_ALL_VARIABLES_WITHIN_SCOPE_QUERY;

@Repository
public interface VariableRepository extends CrudRepository<VariableEntity, String> {

    @Query(FIND_ALL_VARIABLES_WITHIN_SCOPE_QUERY)
    List<VariableEntity> findInScope(@Param("processId") String processId,
                                     @Param("scope") String[] scope);

    @Query(FIND_ALL_PROCESS_VARIABLES_QUERY)
    List<VariableEntity> findInProcess(@Param("processId") String processId);

}
