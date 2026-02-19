package com.leorces.persistence.postgres.repository;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.postgres.entity.ProcessDefinitionEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.leorces.persistence.postgres.repository.query.definition.COUNT_ALL_WITH_FILTERS.COUNT_ALL_WITH_FILTERS_QUERY;
import static com.leorces.persistence.postgres.repository.query.definition.FIND_ALL_WITH_PAGINATION.FIND_ALL_WITH_PAGINATION_QUERY;
import static com.leorces.persistence.postgres.repository.query.definition.FIND_BY_KEY_AND_VERSION.FIND_BY_KEY_AND_VERSION_QUERY;
import static com.leorces.persistence.postgres.repository.query.definition.FIND_FULL_BY_ID.FIND_FULL_BY_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.definition.FIND_LATEST_BY_KEY.FIND_LATEST_BY_KEY_QUERY;
import static com.leorces.persistence.postgres.repository.query.definition.RESUME_BY_ID.RESUME_BY_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.definition.RESUME_BY_KEY.RESUME_BY_KEY_QUERY;
import static com.leorces.persistence.postgres.repository.query.definition.SUSPEND_BY_ID.SUSPEND_BY_ID_QUERY;
import static com.leorces.persistence.postgres.repository.query.definition.SUSPEND_BY_KEY.SUSPEND_BY_KEY_QUERY;

@Repository
public interface DefinitionRepository extends CrudRepository<ProcessDefinitionEntity, String> {

    @Query(FIND_FULL_BY_ID_QUERY)
    Optional<ProcessDefinitionEntity> findFullById(@Param("definitionId") String id);

    @Query(FIND_LATEST_BY_KEY_QUERY)
    Optional<ProcessDefinitionEntity> findLatestByKey(@Param("definitionKey") String definitionKey);

    @Query(FIND_BY_KEY_AND_VERSION_QUERY)
    Optional<ProcessDefinitionEntity> findByKeyAndVersion(@Param("definitionKey") String definitionKey, @Param("version") Integer version);

    @Query(FIND_ALL_WITH_PAGINATION_QUERY)
    List<ProcessDefinitionEntity> findAllWithPagination(@Param("offset") long offset,
                                                        @Param("limit") int limit,
                                                        @Param("order") String order,
                                                        @Param("sort_by_field") String sortByField,
                                                        @Param("filter") String filter);

    @Query(COUNT_ALL_WITH_FILTERS_QUERY)
    long countAllWithFilters(@Param("filter") String filter);

    @Modifying
    @Query(SUSPEND_BY_ID_QUERY)
    void suspendById(@Param("definitionId") String definitionId);

    @Modifying
    @Query(SUSPEND_BY_KEY_QUERY)
    void suspendByKey(@Param("definitionKey") String definitionKey);

    @Modifying
    @Query(RESUME_BY_ID_QUERY)
    void resumeById(@Param("definitionId") String definitionId);

    @Modifying
    @Query(RESUME_BY_KEY_QUERY)
    void resumeByKey(@Param("definitionKey") String definitionKey);

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
