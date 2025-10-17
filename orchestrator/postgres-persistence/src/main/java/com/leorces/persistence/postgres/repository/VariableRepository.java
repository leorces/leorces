package com.leorces.persistence.postgres.repository;

import com.leorces.persistence.postgres.entity.VariableEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.leorces.persistence.postgres.repository.query.VariableQueries.FIND_ALL_PROCESS_VARIABLES;
import static com.leorces.persistence.postgres.repository.query.VariableQueries.FIND_ALL_VARIABLES_WITHIN_SCOPE;

@Repository
public interface VariableRepository extends CrudRepository<VariableEntity, String> {

    @Query(FIND_ALL_VARIABLES_WITHIN_SCOPE)
    List<VariableEntity> findInScope(@Param("processId") String processId,
                                     @Param("scope") List<String> scope);

    @Query(FIND_ALL_PROCESS_VARIABLES)
    List<VariableEntity> findInProcess(@Param("processId") String processId);

}
