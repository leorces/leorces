package com.leorces.persistence.postgres.repository;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.postgres.entity.ProcessDefinitionEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.leorces.persistence.postgres.repository.query.DefinitionQueries.*;

@Repository
public interface DefinitionRepository extends CrudRepository<ProcessDefinitionEntity, String> {

    @Query(FIND_LATEST_BY_KEY)
    Optional<ProcessDefinitionEntity> findLatestByKey(@Param("definitionKey") String definitionKey);

    @Query(FIND_BY_KEY_AND_VERSION)
    Optional<ProcessDefinitionEntity> findByKeyAndVersion(@Param("definitionKey") String definitionKey, @Param("version") Integer version);

    @Query(FIND_ALL_WITH_PAGINATION)
    List<ProcessDefinitionEntity> findAllWithPagination(@Param("offset") long offset,
                                                        @Param("limit") int limit,
                                                        @Param("order") String order,
                                                        @Param("sort_by_field") String sortByField,
                                                        @Param("filter") String filter);

    @Query(COUNT_ALL_WITH_FILTERS)
    long countAllWithFilters(@Param("filter") String filter);

    default PageableData<ProcessDefinitionEntity> findAll(Pageable pageable) {
        var order = pageable.order() != null ? pageable.order().name() : "DESC";
        var sortByField = pageable.sortByField() != null ? pageable.sortByField() : "created_at";
        var filter = pageable.filter() != null ? pageable.filter() : "";

        var data = findAllWithPagination(
                pageable.offset(),
                pageable.limit(),
                order,
                sortByField,
                filter
        );
        var total = countAllWithFilters(filter);

        return new PageableData<>(data, total);
    }

}
