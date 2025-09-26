package com.leorces.model.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pageable Tests")
class PageableTest {

    private static final long TEST_OFFSET = 10L;
    private static final int TEST_LIMIT = 20;
    private static final String TEST_FILTER = "status=active";
    private static final String TEST_STATE = "RUNNING";
    private static final String TEST_SORT_FIELD = "createdAt";
    private static final Pageable.Direction TEST_ORDER = Pageable.Direction.ASC;

    @Test
    @DisplayName("Should create Pageable with all fields using builder")
    void shouldCreatePageableWithAllFields() {
        // When
        var pageable = Pageable.builder()
                .offset(TEST_OFFSET)
                .limit(TEST_LIMIT)
                .filter(TEST_FILTER)
                .state(TEST_STATE)
                .sortByField(TEST_SORT_FIELD)
                .order(TEST_ORDER)
                .build();

        // Then
        assertNotNull(pageable);
        assertEquals(TEST_OFFSET, pageable.offset());
        assertEquals(TEST_LIMIT, pageable.limit());
        assertEquals(TEST_FILTER, pageable.filter());
        assertEquals(TEST_STATE, pageable.state());
        assertEquals(TEST_SORT_FIELD, pageable.sortByField());
        assertEquals(TEST_ORDER, pageable.order());
    }

    @Test
    @DisplayName("Should create Pageable with default constructor")
    void shouldCreatePageableWithDefaultConstructor() {
        // When
        var pageable = new Pageable();

        // Then
        assertNotNull(pageable);
        assertEquals(0L, pageable.offset());
        assertEquals(0, pageable.limit());
        assertEquals("", pageable.filter());
        assertEquals("", pageable.state());
        assertNull(pageable.sortByField());
        assertNull(pageable.order());
    }

    @Test
    @DisplayName("Should create Pageable with offset and limit constructor")
    void shouldCreatePageableWithOffsetAndLimitConstructor() {
        // When
        var pageable = new Pageable(TEST_OFFSET, TEST_LIMIT);

        // Then
        assertNotNull(pageable);
        assertEquals(TEST_OFFSET, pageable.offset());
        assertEquals(TEST_LIMIT, pageable.limit());
        assertEquals("", pageable.filter());
        assertEquals("", pageable.state());
        assertNull(pageable.sortByField());
        assertNull(pageable.order());
    }

    @Test
    @DisplayName("Should create Pageable with offset, limit and filter constructor")
    void shouldCreatePageableWithOffsetLimitFilterConstructor() {
        // When
        var pageable = new Pageable(TEST_OFFSET, TEST_LIMIT, TEST_FILTER);

        // Then
        assertNotNull(pageable);
        assertEquals(TEST_OFFSET, pageable.offset());
        assertEquals(TEST_LIMIT, pageable.limit());
        assertEquals(TEST_FILTER, pageable.filter());
        assertEquals("", pageable.state());
        assertNull(pageable.sortByField());
        assertNull(pageable.order());
    }

    @Test
    @DisplayName("Should create Pageable with offset, limit, filter and state constructor")
    void shouldCreatePageableWithOffsetLimitFilterStateConstructor() {
        // When
        var pageable = new Pageable(TEST_OFFSET, TEST_LIMIT, TEST_FILTER, TEST_STATE);

        // Then
        assertNotNull(pageable);
        assertEquals(TEST_OFFSET, pageable.offset());
        assertEquals(TEST_LIMIT, pageable.limit());
        assertEquals(TEST_FILTER, pageable.filter());
        assertEquals(TEST_STATE, pageable.state());
        assertNull(pageable.sortByField());
        assertNull(pageable.order());
    }

    @Test
    @DisplayName("Should support toBuilder functionality")
    void shouldSupportToBuilderFunctionality() {
        // Given
        var originalPageable = new Pageable(TEST_OFFSET, TEST_LIMIT);

        // When
        var modifiedPageable = originalPageable.toBuilder()
                .filter(TEST_FILTER)
                .order(TEST_ORDER)
                .build();

        // Then
        assertEquals(TEST_OFFSET, modifiedPageable.offset());
        assertEquals(TEST_LIMIT, modifiedPageable.limit());
        assertEquals(TEST_FILTER, modifiedPageable.filter());
        assertEquals(TEST_ORDER, modifiedPageable.order());
    }

    // Direction enum tests
    @Test
    @DisplayName("Should contain both ASC and DESC directions")
    void shouldContainBothDirections() {
        // Given
        var directions = Pageable.Direction.values();

        // When & Then
        assertEquals(2, directions.length);
        assertTrue(contains(directions, Pageable.Direction.ASC));
        assertTrue(contains(directions, Pageable.Direction.DESC));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ASC", "asc", "Asc"})
    @DisplayName("Should parse ASC direction from string")
    void shouldParseAscDirectionFromString(String value) {
        // When
        var direction = Pageable.Direction.fromString(value);

        // Then
        assertEquals(Pageable.Direction.ASC, direction);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DESC", "desc", "Desc"})
    @DisplayName("Should parse DESC direction from string")
    void shouldParseDescDirectionFromString(String value) {
        // When
        var direction = Pageable.Direction.fromString(value);

        // Then
        assertEquals(Pageable.Direction.DESC, direction);
    }

    @Test
    @DisplayName("Should throw exception for invalid direction string")
    void shouldThrowExceptionForInvalidDirectionString() {
        // Given
        var invalidValue = "INVALID";

        // When & Then
        var exception = assertThrows(IllegalArgumentException.class,
                () -> Pageable.Direction.fromString(invalidValue));
        assertTrue(exception.getMessage().contains("Invalid value"));
        assertTrue(exception.getMessage().contains(invalidValue));
    }

    @Test
    @DisplayName("Should correctly identify ascending direction")
    void shouldCorrectlyIdentifyAscendingDirection() {
        // When & Then
        assertTrue(Pageable.Direction.ASC.isAscending());
        assertFalse(Pageable.Direction.DESC.isAscending());
    }

    @Test
    @DisplayName("Should correctly identify descending direction")
    void shouldCorrectlyIdentifyDescendingDirection() {
        // When & Then
        assertTrue(Pageable.Direction.DESC.isDescending());
        assertFalse(Pageable.Direction.ASC.isDescending());
    }

    @ParameterizedTest
    @EnumSource(Pageable.Direction.class)
    @DisplayName("Should have valid toString for all directions")
    void shouldHaveValidToStringForAllDirections(Pageable.Direction direction) {
        // When
        var toString = direction.toString();

        // Then
        assertNotNull(toString);
        assertEquals(direction.name(), toString);
    }

    private boolean contains(Pageable.Direction[] directions, Pageable.Direction target) {
        for (Pageable.Direction direction : directions) {
            if (direction == target) {
                return true;
            }
        }
        return false;
    }

}