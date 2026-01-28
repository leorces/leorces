package com.leorces.persistence.postgres.repository;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.postgres.entity.JobEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.leorces.persistence.postgres.repository.query.job.COUNT_ALL_WITH_FILTERS.COUNT_ALL_WITH_FILTERS_QUERY;
import static com.leorces.persistence.postgres.repository.query.job.FIND_ALL_WITH_PAGINATION.FIND_ALL_WITH_PAGINATION_QUERY;

@Repository
public interface JobRepository extends CrudRepository<JobEntity, String> {

    @Query(FIND_ALL_WITH_PAGINATION_QUERY)
    List<JobEntity> findAllWithPagination(@Param("offset") long offset,
                                          @Param("limit") int limit,
                                          @Param("order") String order,
                                          @Param("sort_by_field") String sortByField,
                                          @Param("filter") String filter,
                                          @Param("state") String state);

    @Query(COUNT_ALL_WITH_FILTERS_QUERY)
    long countAllWithFilters(@Param("filter") String filter, @Param("state") String state);

    default PageableData<JobEntity> findAll(Pageable pageable) {
        var orderName = pageable.order() != null ? pageable.order().name() : "DESC";
        var sortField = pageable.sortByField() != null ? pageable.sortByField() : "job_created_at";

        var data = findAllWithPagination(
                pageable.offset(),
                pageable.limit(),
                orderName,
                sortField,
                pageable.filter(),
                pageable.state()
        );

        var total = countAllWithFilters(pageable.filter(), pageable.state());
        return new PageableData<>(data, total);
    }

}
