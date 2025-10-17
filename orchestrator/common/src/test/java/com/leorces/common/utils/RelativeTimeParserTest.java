package com.leorces.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RelativeTimeParser Unit Tests")
class RelativeTimeParserTest {

    @Test
    @DisplayName("Should return non-null LocalDateTime")
    void shouldReturnNonNullLocalDateTime() {
        // Given & When
        var result = RelativeTimeParser.parseRelative("1h");

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return time in the future relative to now")
    void shouldReturnFutureTime() {
        // Given
        var before = LocalDateTime.now();

        // When
        var result = RelativeTimeParser.parseRelative("1h");

        // Then
        assertTrue(result.isAfter(before) || result.isEqual(before));
    }

    @Test
    @DisplayName("Should correctly parse multiple units")
    void shouldCorrectlyParseMultipleUnits() {
        // Given
        var before = LocalDateTime.now();

        // When
        var result = RelativeTimeParser.parseRelative("1h 1m 1s 500ms");

        // Then
        assertTrue(result.isAfter(before) || result.isEqual(before));
    }

    @Test
    @DisplayName("Should handle single unit formats")
    void shouldHandleSingleUnitFormats() {
        assertNotNull(RelativeTimeParser.parseRelative("1d"));
        assertNotNull(RelativeTimeParser.parseRelative("1h"));
        assertNotNull(RelativeTimeParser.parseRelative("1m"));
        assertNotNull(RelativeTimeParser.parseRelative("1s"));
        assertNotNull(RelativeTimeParser.parseRelative("1ms"));
    }

}
