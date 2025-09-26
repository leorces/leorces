package com.leorces.model.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pageable Data Tests")
class PageableDataTest {

    private static final List<String> TEST_DATA = List.of("item1", "item2", "item3");
    private static final long TEST_TOTAL = 100L;

    @Test
    @DisplayName("Should create PageableData with data and total")
    void shouldCreatePageableDataWithDataAndTotal() {
        // When
        var pageableData = new PageableData<>(TEST_DATA, TEST_TOTAL);

        // Then
        assertNotNull(pageableData);
        assertEquals(TEST_DATA, pageableData.data());
        assertEquals(TEST_TOTAL, pageableData.total());
    }

    @Test
    @DisplayName("Should create PageableData with null data")
    void shouldCreatePageableDataWithNullData() {
        // When
        var pageableData = new PageableData<String>(null, TEST_TOTAL);

        // Then
        assertNotNull(pageableData);
        assertNull(pageableData.data());
        assertEquals(TEST_TOTAL, pageableData.total());
    }

    @Test
    @DisplayName("Should create PageableData with empty list")
    void shouldCreatePageableDataWithEmptyList() {
        // Given
        var emptyData = List.<String>of();

        // When
        var pageableData = new PageableData<>(emptyData, 0L);

        // Then
        assertNotNull(pageableData);
        assertEquals(emptyData, pageableData.data());
        assertTrue(pageableData.data().isEmpty());
        assertEquals(0L, pageableData.total());
    }

    @Test
    @DisplayName("Should create PageableData with different generic types")
    void shouldCreatePageableDataWithDifferentGenericTypes() {
        // Given
        var intData = List.of(1, 2, 3);
        var stringData = List.of("a", "b", "c");

        // When
        var intPageableData = new PageableData<>(intData, 50L);
        var stringPageableData = new PageableData<>(stringData, 75L);

        // Then
        assertNotNull(intPageableData);
        assertNotNull(stringPageableData);
        assertEquals(intData, intPageableData.data());
        assertEquals(stringData, stringPageableData.data());
        assertEquals(50L, intPageableData.total());
        assertEquals(75L, stringPageableData.total());
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        var pageableData1 = new PageableData<>(TEST_DATA, TEST_TOTAL);
        var pageableData2 = new PageableData<>(TEST_DATA, TEST_TOTAL);
        var pageableData3 = new PageableData<>(List.of("different"), TEST_TOTAL);

        // When & Then
        assertEquals(pageableData1, pageableData2);
        assertNotEquals(pageableData1, pageableData3);
        assertNotEquals(null, pageableData1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        // Given
        var pageableData1 = new PageableData<>(TEST_DATA, TEST_TOTAL);
        var pageableData2 = new PageableData<>(TEST_DATA, TEST_TOTAL);

        // When & Then
        assertEquals(pageableData1.hashCode(), pageableData2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        var pageableData = new PageableData<>(TEST_DATA, TEST_TOTAL);

        // When
        var toStringResult = pageableData.toString();

        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("PageableData"));
        assertTrue(toStringResult.contains(String.valueOf(TEST_TOTAL)));
    }

    @Test
    @DisplayName("Should handle zero total")
    void shouldHandleZeroTotal() {
        // When
        var pageableData = new PageableData<>(TEST_DATA, 0L);

        // Then
        assertNotNull(pageableData);
        assertEquals(TEST_DATA, pageableData.data());
        assertEquals(0L, pageableData.total());
    }

    @Test
    @DisplayName("Should handle negative total")
    void shouldHandleNegativeTotal() {
        // Given
        var negativeTotal = -5L;

        // When
        var pageableData = new PageableData<>(TEST_DATA, negativeTotal);

        // Then
        assertNotNull(pageableData);
        assertEquals(TEST_DATA, pageableData.data());
        assertEquals(negativeTotal, pageableData.total());
    }

}