package com.leorces.persistence.postgres.repository;

import com.leorces.persistence.postgres.entity.VariableEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.leorces.persistence.postgres.repository.query.VariableQueries.FIND_ALL;

@Repository
public interface VariableRepository extends CrudRepository<VariableEntity, String> {

    @Query(FIND_ALL)
    List<VariableEntity> findAll(@Param("processId") String processId,
                                 @Param("scope") List<String> scope);

}
