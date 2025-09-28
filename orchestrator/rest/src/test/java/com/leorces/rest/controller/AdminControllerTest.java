package com.leorces.rest.controller;

import com.leorces.api.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController Tests")
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    private AdminController subject;

    @BeforeEach
    void setUp() {
        subject = new AdminController(adminService);
    }

    @Test
    @DisplayName("Should perform repository compaction successfully")
    void shouldPerformRepositoryCompactionSuccessfully() {
        // When
        subject.compaction();

        // Then
        verify(adminService).doCompaction();
    }

    @Test
    @DisplayName("Should call doCompaction method exactly once")
    void shouldCallDoCompactionMethodExactlyOnce() {
        // When
        subject.compaction();

        // Then
        verify(adminService, times(1)).doCompaction();
        verifyNoMoreInteractions(adminService);
    }

    @Test
    @DisplayName("Should handle multiple consecutive compaction calls")
    void shouldHandleMultipleConsecutiveCompactionCalls() {
        // When
        subject.compaction();
        subject.compaction();
        subject.compaction();

        // Then
        verify(adminService, times(3)).doCompaction();
    }

    @Test
    @DisplayName("Should not throw exception during compaction")
    void shouldNotThrowExceptionDuringCompaction() {
        // Given
        doNothing().when(adminService).doCompaction();

        // When & Then
        subject.compaction();

        verify(adminService).doCompaction();
    }

    @Test
    @DisplayName("Should propagate exception from repository service")
    void shouldPropagateExceptionFromRepositoryService() {
        // Given
        var expectedException = new RuntimeException("Compaction failed");
        doThrow(expectedException).when(adminService).doCompaction();

        // When & Then
        try {
            subject.compaction();
        } catch (RuntimeException e) {
            // Exception should be propagated as-is
            verify(adminService).doCompaction();
        }
    }

}