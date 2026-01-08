package com.leorces.persistence.postgres.repository.query;

public final class DefinitionQueries {

    public static final String FIND_LATEST_BY_KEY = """
            SELECT *
            FROM definition
            WHERE definition_key = :definitionKey
            ORDER BY definition_key, definition_version DESC
            LIMIT 1;
            """;

    public static final String FIND_BY_KEY_AND_VERSION = """
            SELECT *
            FROM definition
            WHERE definition_key = :definitionKey
              AND definition_version = :version;
            """;

    public static final String FIND_ALL_WITH_PAGINATION = """
            WITH filtered AS (
                SELECT definition.*
                FROM definition
                WHERE (:filter IS NULL OR :filter = '' OR
                       LOWER(definition.definition_id) LIKE LOWER(CONCAT('%', :filter, '%')) OR
                       LOWER(definition.definition_key) LIKE LOWER(CONCAT('%', :filter, '%')) OR
                       LOWER(definition.definition_name) LIKE LOWER(CONCAT('%', :filter, '%')))
            ),
            keys_page AS (
                SELECT DISTINCT ON (filtered.definition_key) filtered.definition_key
                FROM filtered
                ORDER BY
                    filtered.definition_key,
                    CASE WHEN :sort_by_field = 'created_at'  AND :order = 'ASC'  THEN filtered.definition_created_at END ASC,
                    CASE WHEN :sort_by_field = 'created_at'  AND :order = 'DESC' THEN filtered.definition_created_at END DESC,
                    CASE WHEN :sort_by_field = 'updated_at'  AND :order = 'ASC'  THEN filtered.definition_updated_at END ASC,
                    CASE WHEN :sort_by_field = 'updated_at'  AND :order = 'DESC' THEN filtered.definition_updated_at END DESC,
                    CASE WHEN :sort_by_field = 'definition_key'  AND :order = 'ASC'  THEN filtered.definition_key END ASC,
                    CASE WHEN :sort_by_field = 'definition_key'  AND :order = 'DESC' THEN filtered.definition_key END DESC,
                    CASE WHEN :sort_by_field = 'definition_name' AND :order = 'ASC'  THEN filtered.definition_name END ASC,
                    CASE WHEN :sort_by_field = 'definition_name' AND :order = 'DESC' THEN filtered.definition_name END DESC,
                    filtered.definition_created_at DESC
                OFFSET :offset
                LIMIT :limit
            )
            SELECT filtered.*
            FROM filtered
            JOIN keys_page ON filtered.definition_key = keys_page.definition_key
            ORDER BY
                CASE WHEN :sort_by_field = 'created_at'  AND :order = 'ASC'  THEN filtered.definition_created_at END ASC,
                CASE WHEN :sort_by_field = 'created_at'  AND :order = 'DESC' THEN filtered.definition_created_at END DESC,
                CASE WHEN :sort_by_field = 'updated_at'  AND :order = 'ASC'  THEN filtered.definition_updated_at END ASC,
                CASE WHEN :sort_by_field = 'updated_at'  AND :order = 'DESC' THEN filtered.definition_updated_at END DESC,
                CASE WHEN :sort_by_field = 'definition_key'  AND :order = 'ASC'  THEN filtered.definition_key END ASC,
                CASE WHEN :sort_by_field = 'definition_key'  AND :order = 'DESC' THEN filtered.definition_key END DESC,
                CASE WHEN :sort_by_field = 'definition_name' AND :order = 'ASC'  THEN filtered.definition_name END ASC,
                CASE WHEN :sort_by_field = 'definition_name' AND :order = 'DESC' THEN filtered.definition_name END DESC,
                filtered.definition_created_at DESC;
            """;

    public static final String COUNT_ALL_WITH_FILTERS = """
            SELECT COUNT(*)
            FROM definition
            WHERE (:filter IS NULL OR :filter = '' OR
                   LOWER(definition.definition_id) LIKE LOWER(CONCAT('%', :filter, '%')) OR
                   LOWER(definition.definition_key) LIKE LOWER(CONCAT('%', :filter, '%')) OR
                   LOWER(definition.definition_name) LIKE LOWER(CONCAT('%', :filter, '%')))
            """;

    public static final String SUSPEND_BY_ID = """
            INSERT INTO definition_suspended (definition_id, definition_suspended)
            VALUES (:definitionId, TRUE)
            ON CONFLICT (definition_id)
            DO UPDATE
            SET definition_suspended = TRUE;
            """;

    public static final String SUSPEND_BY_KEY = """
            INSERT INTO definition_suspended (definition_id, definition_suspended)
            SELECT d.definition_id, TRUE
            FROM definition d
            WHERE d.definition_key = :definitionKey
            ON CONFLICT (definition_id)
            DO UPDATE
            SET definition_suspended = TRUE;
            """;

    public static final String RESUME_BY_ID = """
            INSERT INTO definition_suspended (definition_id, definition_suspended)
            VALUES (:definitionId, FALSE)
            ON CONFLICT (definition_id)
            DO UPDATE
            SET definition_suspended = FALSE;
            """;

    public static final String RESUME_BY_KEY = """
            INSERT INTO definition_suspended (definition_id, definition_suspended)
            SELECT d.definition_id, FALSE
            FROM definition d
            WHERE d.definition_key = :definitionKey
            ON CONFLICT (definition_id)
            DO UPDATE
            SET definition_suspended = FALSE;
            """;

    private DefinitionQueries() {
        // Utility class
    }

}