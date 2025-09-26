package com.leorces.persistence.postgres.repository;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.postgres.entity.HistoryEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.leorces.persistence.postgres.repository.query.HistoryQueries.FIND_ALL_WITH_PAGINATION;

@Repository
public interface HistoryRepository extends CrudRepository<HistoryEntity, String> {

    @Query(FIND_ALL_WITH_PAGINATION)
    List<HistoryEntity> findAllWithPagination(@Param("offset") long offset,
                                              @Param("limit") int limit);

    default PageableData<HistoryEntity> findAll(Pageable pageable) {
        var data = findAllWithPagination(pageable.offset(), pageable.limit());
        var total = count();

        return new PageableData<>(data, total);
    }

}
