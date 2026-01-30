package com.leorces.persistence.postgres.repository.query.definition;

public final class COUNT_ALL_WITH_FILTERS {

    public static final String COUNT_ALL_WITH_FILTERS_QUERY = """
            SELECT COUNT(*)
            FROM definition
            WHERE (:filter IS NULL OR :filter = '' OR
                   (definition_id || ' ' || definition_key || ' ' || definition_name) ILIKE CONCAT('%', :filter, '%'))
            """;

    private COUNT_ALL_WITH_FILTERS() {
        // Index: idx_definition_search_trgm
    }

}
