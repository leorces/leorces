package com.leorces.persistence.postgres.repository.query.definition;

public final class FIND_ALL_WITH_PAGINATION {

    public static final String FIND_ALL_WITH_PAGINATION_QUERY = """
            WITH filtered AS (SELECT definition.*
                              FROM definition
                              WHERE (
                                        :filter IS NULL
                                            OR :filter = ''
                                            OR (definition_id || ' ' || definition_key || ' ' || definition_name)
                                            ILIKE CONCAT('%', :filter, '%')
                                        )),
                 keys_page AS (SELECT DISTINCT ON (filtered.definition_key) filtered.definition_key
                               FROM filtered
                               ORDER BY filtered.definition_key,
                                        CASE
                                            WHEN :sort_by_field = 'definition_key' AND :order = 'ASC'
                                                THEN filtered.definition_key END ASC,
                                        CASE
                                            WHEN :sort_by_field = 'definition_key' AND :order = 'DESC'
                                                THEN filtered.definition_key END DESC,
                                        CASE
                                            WHEN :sort_by_field = 'definition_name' AND :order = 'ASC'
                                                THEN filtered.definition_name END ASC,
                                        CASE
                                            WHEN :sort_by_field = 'definition_name' AND :order = 'DESC'
                                                THEN filtered.definition_name END DESC,
                                        filtered.definition_key
                               OFFSET :offset LIMIT :limit)
            SELECT filtered.*
            FROM filtered
                     JOIN keys_page
                          ON filtered.definition_key = keys_page.definition_key
            ORDER BY CASE
                         WHEN :sort_by_field = 'definition_key' AND :order = 'ASC'
                             THEN filtered.definition_key END ASC,
                     CASE
                         WHEN :sort_by_field = 'definition_key' AND :order = 'DESC'
                             THEN filtered.definition_key END DESC,
                     CASE
                         WHEN :sort_by_field = 'definition_name' AND :order = 'ASC'
                             THEN filtered.definition_name END ASC,
                     CASE
                         WHEN :sort_by_field = 'definition_name' AND :order = 'DESC'
                             THEN filtered.definition_name END DESC,
                     filtered.definition_key;
            """;

    private FIND_ALL_WITH_PAGINATION() {
        // Index: idx_definition_search_trgm
    }

}
