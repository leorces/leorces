package com.leorces.engine.service.activity;

import com.leorces.api.exception.ExecutionException;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ActivityFactory Tests")
class ActivityFactoryTest {

    private static final String ACTIVITY_ID = "activity-123";
    private static final String DEFINITION_ID = "definition-456";
    private static final String PROCESS_ID = "process-789";

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ActivityDefinition activityDefinition;

    @Mock
    private Process process;

    @Mock
    private ProcessDefinition processDefinition;

    @Mock
    private ActivityExecution activityExecution;

    @InjectMocks
    private ActivityFactory activityFactory;

    @BeforeEach
    void setUp() {
        when(activityDefinition.id()).thenReturn(DEFINITION_ID);
        when(process.id()).thenReturn(PROCESS_ID);
        when(process.definition()).thenReturn(processDefinition);
    }

    @Test
    @DisplayName("Should create activity execution from definition and process")
    void shouldCreateActivityExecutionFromDefinitionAndProcess() {
        // When
        var result = activityFactory.createActivity(activityDefinition, process);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.definitionId()).isEqualTo(DEFINITION_ID);
        assertThat(result.process()).isEqualTo(process);
    }

    @Test
    @DisplayName("Should get activity by ID successfully")
    void shouldGetActivityByIdSuccessfully() {
        // Given
        when(activityPersistence.findById(ACTIVITY_ID)).thenReturn(Optional.of(activityExecution));

        // When
        var result = activityFactory.getById(ACTIVITY_ID);

        // Then
        assertThat(result).isEqualTo(activityExecution);
        verify(activityPersistence).findById(ACTIVITY_ID);
    }

    @Test
    @DisplayName("Should throw ActivityNotFoundException when activity not found by ID")
    void shouldThrowActivityNotFoundExceptionWhenActivityNotFoundById() {
        // Given
        when(activityPersistence.findById(ACTIVITY_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> activityFactory.getById(ACTIVITY_ID))
                .isInstanceOf(ExecutionException.class);
        verify(activityPersistence).findById(ACTIVITY_ID);
    }

    @Test
    @DisplayName("Should get new activity by definition ID successfully")
    void shouldGetNewActivityByDefinitionIdSuccessfully() {
        // Given
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(process));
        when(processDefinition.getActivityById(DEFINITION_ID)).thenReturn(Optional.of(activityDefinition));

        // When
        var result = activityFactory.getNewByDefinitionId(DEFINITION_ID, PROCESS_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.definitionId()).isEqualTo(DEFINITION_ID);
        assertThat(result.process()).isEqualTo(process);
        verify(processPersistence).findById(PROCESS_ID);
        verify(processDefinition).getActivityById(DEFINITION_ID);
    }

    @Test
    @DisplayName("Should throw ProcessNotFoundException when process not found")
    void shouldThrowProcessNotFoundExceptionWhenProcessNotFound() {
        // Given
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> activityFactory.getNewByDefinitionId(DEFINITION_ID, PROCESS_ID))
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Process not found");
        verify(processPersistence).findById(PROCESS_ID);
    }

    @Test
    @DisplayName("Should throw ActivityNotFoundException when activity definition not found in process")
    void shouldThrowActivityNotFoundExceptionWhenDefinitionNotFoundInProcess() {
        // Given
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(process));
        when(processDefinition.getActivityById(DEFINITION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> activityFactory.getNewByDefinitionId(DEFINITION_ID, PROCESS_ID))
                .isInstanceOf(ExecutionException.class);
        verify(processPersistence).findById(PROCESS_ID);
        verify(processDefinition).getActivityById(DEFINITION_ID);
    }

    @Test
    @DisplayName("Should get activity by definition ID and process ID successfully")
    void shouldGetActivityByDefinitionIdAndProcessIdSuccessfully() {
        // Given
        when(activityPersistence.findByDefinitionId(PROCESS_ID, DEFINITION_ID))
                .thenReturn(Optional.of(activityExecution));

        // When
        var result = activityFactory.getByDefinitionId(DEFINITION_ID, PROCESS_ID);

        // Then
        assertThat(result).isEqualTo(activityExecution);
        verify(activityPersistence).findByDefinitionId(PROCESS_ID, DEFINITION_ID);
    }

    @Test
    @DisplayName("Should throw ActivityNotFoundException when activity not found by definition ID and process ID")
    void shouldThrowActivityNotFoundExceptionWhenActivityNotFoundByDefinitionIdAndProcessId() {
        // Given
        when(activityPersistence.findByDefinitionId(PROCESS_ID, DEFINITION_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> activityFactory.getByDefinitionId(DEFINITION_ID, PROCESS_ID))
                .isInstanceOf(ExecutionException.class);
        verify(activityPersistence).findByDefinitionId(PROCESS_ID, DEFINITION_ID);
    }

}