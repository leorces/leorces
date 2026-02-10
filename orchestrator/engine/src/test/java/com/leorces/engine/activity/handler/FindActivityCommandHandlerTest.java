package com.leorces.engine.activity.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.FindActivityCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.persistence.ActivityPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindActivityCommandHandler Tests")
class FindActivityCommandHandlerTest {

    private static final String ACTIVITY_ID = "activity-123";
    private static final String DEFINITION_ID = "definition-456";
    private static final String PROCESS_ID = "process-789";

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private ActivityExecution activityExecution;

    @InjectMocks
    private FindActivityCommandHandler handler;

    @Test
    @DisplayName("Should find activity by ID successfully")
    void shouldFindActivityByIdSuccessfully() {
        // Given
        var command = FindActivityCommand.byId(ACTIVITY_ID);
        when(activityPersistence.findById(ACTIVITY_ID)).thenReturn(Optional.of(activityExecution));

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isEqualTo(activityExecution);
        verify(activityPersistence).findById(ACTIVITY_ID);
    }

    @Test
    @DisplayName("Should throw exception when activity not found by ID")
    void shouldThrowExceptionWhenActivityNotFoundById() {
        // Given
        var command = FindActivityCommand.byId(ACTIVITY_ID);
        when(activityPersistence.findById(ACTIVITY_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(ExecutionException.class)
                .extracting("detailedMessage")
                .isEqualTo("Activity with id: %s not found".formatted(ACTIVITY_ID));
    }

    @Test
    @DisplayName("Should find activity by definition ID successfully")
    void shouldFindActivityByDefinitionIdSuccessfully() {
        // Given
        var command = FindActivityCommand.byDefinitionId(PROCESS_ID, DEFINITION_ID);
        when(activityPersistence.findByDefinitionId(PROCESS_ID, DEFINITION_ID)).thenReturn(Optional.of(activityExecution));

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isEqualTo(activityExecution);
        verify(activityPersistence).findByDefinitionId(PROCESS_ID, DEFINITION_ID);
    }

    @Test
    @DisplayName("Should throw exception when activity not found by definition ID")
    void shouldThrowExceptionWhenActivityNotFoundByDefinitionId() {
        // Given
        var command = FindActivityCommand.byDefinitionId(PROCESS_ID, DEFINITION_ID);
        when(activityPersistence.findByDefinitionId(PROCESS_ID, DEFINITION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(ExecutionException.class)
                .extracting("detailedMessage")
                .isEqualTo("Activity not found for process: %s and definition: %s".formatted(PROCESS_ID, DEFINITION_ID));
    }

}
