package com.leorces.common.utils;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DisplayName("Host Utils Unit Tests")
class HostUtilsTest {

    @Test
    @DisplayName("Should have non-null HOSTNAME field")
    void shouldHaveNonNullHostnameField() {
        // Given & When & Then
        assertNotNull(HostUtils.HOSTNAME);
    }

    @Test
    @DisplayName("Should have valid hostname or fallback value")
    void shouldHaveValidHostnameOrFallbackValue() {
        // Given & When & Then
        var hostname = HostUtils.HOSTNAME;
        assertTrue(hostname != null && !hostname.isEmpty());

        // Hostname should either be a valid hostname or the fallback "unknown-host"
        assertTrue(hostname.equals("unknown-host") || hostname.length() > 0);
    }

}