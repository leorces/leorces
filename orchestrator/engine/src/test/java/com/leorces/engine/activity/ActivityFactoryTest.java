package com.leorces.engine.activity;

import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.engine.exception.process.ProcessNotFoundException;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityFactory Tests")
class ActivityFactoryTest {

    private static final String ACTIVITY_ID = "test-activity-id";
    private static final String DEFINITION_ID = "test-definition-id";
    private static final String PROCESS_ID = "test-process-id";
    private static final String NONEXISTENT_ID = "nonexistent-id";

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private ProcessPersistence processPersistence;

    private ActivityFactory activityFactory;

    @BeforeEach
    void setUp() {
        activityFactory = new ActivityFactory(activityPersistence, processPersistence);
    }

    @Test
    @DisplayName("Should create activity from definition and process successfully")
    void shouldCreateActivityFromDefinitionAndProcessSuccessfully() {
        // Given
        var definition = createActivityDefinition();
        var process = createProcess();

        // When
        var result = activityFactory.createActivity(definition, process);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.definitionId()).isEqualTo(DEFINITION_ID);
        assertThat(result.process()).isEqualTo(process);
    }

    @Test
    @DisplayName("Should get activity by ID successfully when exists")
    void shouldGetActivityByIdSuccessfullyWhenExists() {
        // Given
        var expectedActivity = createActivityExecution();

        when(activityPersistence.findById(ACTIVITY_ID)).thenReturn(Optional.of(expectedActivity));

        // When
        var result = activityFactory.getById(ACTIVITY_ID);

        // Then
        assertThat(result).isEqualTo(expectedActivity);
        verify(activityPersistence).findById(ACTIVITY_ID);
    }

    @Test
    @DisplayName("Should throw ActivityNotFoundException when activity not found by ID")
    void shouldThrowActivityNotFoundExceptionWhenActivityNotFoundById() {
        // Given
        when(activityPersistence.findById(NONEXISTENT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> activityFactory.getById(NONEXISTENT_ID))
                .isInstanceOf(ActivityNotFoundException.class);

        verify(activityPersistence).findById(NONEXISTENT_ID);
    }

    @Test
    @DisplayName("Should get new activity by definition ID successfully")
    void shouldGetNewActivityByDefinitionIdSuccessfully() {
        // Given
        var process = createProcess();
        var definition = createActivityDefinition();

        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(process));
        when(process.definition().getActivityById(DEFINITION_ID)).thenReturn(Optional.of(definition));

        // When
        var result = activityFactory.getNewByDefinitionId(DEFINITION_ID, PROCESS_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.definitionId()).isEqualTo(DEFINITION_ID);
        assertThat(result.process()).isEqualTo(process);
        verify(processPersistence).findById(PROCESS_ID);
    }

    @Test
    @DisplayName("Should throw ProcessNotFoundException when process not found for new activity")
    void shouldThrowProcessNotFoundExceptionWhenProcessNotFoundForNewActivity() {
        // Given
        when(processPersistence.findById(NONEXISTENT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> activityFactory.getNewByDefinitionId(DEFINITION_ID, NONEXISTENT_ID))
                .isInstanceOf(ProcessNotFoundException.class);

        verify(processPersistence).findById(NONEXISTENT_ID);
    }

    @Test
    @DisplayName("Should throw ActivityNotFoundException when activity definition not found in process")
    void shouldThrowActivityNotFoundExceptionWhenActivityDefinitionNotFoundInProcess() {
        // Given
        var process = createProcess();

        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(process));
        when(process.definition().getActivityById(NONEXISTENT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> activityFactory.getNewByDefinitionId(NONEXISTENT_ID, PROCESS_ID))
                .isInstanceOf(ActivityNotFoundException.class);

        verify(processPersistence).findById(PROCESS_ID);
    }

    @Test
    @DisplayName("Should get activity by definition ID successfully when exists")
    void shouldGetActivityByDefinitionIdSuccessfullyWhenExists() {
        // Given
        var expectedActivity = createActivityExecution();

        when(activityPersistence.findByDefinitionId(PROCESS_ID, DEFINITION_ID))
                .thenReturn(Optional.of(expectedActivity));

        // When
        var result = activityFactory.getByDefinitionId(DEFINITION_ID, PROCESS_ID);

        // Then
        assertThat(result).isEqualTo(expectedActivity);
        verify(activityPersistence).findByDefinitionId(PROCESS_ID, DEFINITION_ID);
    }

    @Test
    @DisplayName("Should throw ActivityNotFoundException when activity not found by definition ID")
    void shouldThrowActivityNotFoundExceptionWhenActivityNotFoundByDefinitionId() {
        // Given
        when(activityPersistence.findByDefinitionId(PROCESS_ID, NONEXISTENT_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> activityFactory.getByDefinitionId(NONEXISTENT_ID, PROCESS_ID))
                .isInstanceOf(ActivityNotFoundException.class);

        verify(activityPersistence).findByDefinitionId(PROCESS_ID, NONEXISTENT_ID);
    }

    @Test
    @DisplayName("Should create activity with correct definition ID and process")
    void shouldCreateActivityWithCorrectDefinitionIdAndProcess() {
        // Given
        var definition = createActivityDefinition();
        var process = createProcess();

        // When
        var result = activityFactory.createActivity(definition, process);

        // Then
        assertThat(result.definitionId()).isEqualTo(definition.id());
        assertThat(result.process()).isEqualTo(process);
        assertThat(result.process().id()).isEqualTo(PROCESS_ID);
    }

    @Test
    @DisplayName("Should handle null process gracefully in createActivity")
    void shouldHandleNullProcessGracefullyInCreateActivity() {
        // Given
        var definition = createActivityDefinition();

        // When
        var result = activityFactory.createActivity(definition, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.definitionId()).isEqualTo(DEFINITION_ID);
        assertThat(result.process()).isNull();
    }

    @Test
    @DisplayName("Should throw NullPointerException when definition is null")
    void shouldThrowNullPointerExceptionWhenDefinitionIsNull() {
        // Given
        var process = createProcess();

        // When & Then
        assertThatThrownBy(() -> activityFactory.createActivity(null, process))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should verify correct parameters passed to persistence methods")
    void shouldVerifyCorrectParametersPassedToPersistenceMethods() {
        // Given
        var activity = createActivityExecution();

        when(activityPersistence.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
        when(activityPersistence.findByDefinitionId(PROCESS_ID, DEFINITION_ID))
                .thenReturn(Optional.of(activity));

        // When
        activityFactory.getById(ACTIVITY_ID);
        activityFactory.getByDefinitionId(DEFINITION_ID, PROCESS_ID);

        // Then
        verify(activityPersistence).findById(ACTIVITY_ID);
        verify(activityPersistence).findByDefinitionId(PROCESS_ID, DEFINITION_ID);
    }

    private ActivityDefinition createActivityDefinition() {
        var mockDefinition = mock(ActivityDefinition.class, withSettings().lenient());
        when(mockDefinition.id()).thenReturn(DEFINITION_ID);
        when(mockDefinition.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        return mockDefinition;
    }

    private Process createProcess() {
        var processDefinition = createProcessDefinition();
        var mockProcess = mock(Process.class, withSettings().lenient());
        when(mockProcess.id()).thenReturn(PROCESS_ID);
        when(mockProcess.state()).thenReturn(ProcessState.ACTIVE);
        when(mockProcess.definition()).thenReturn(processDefinition);
        return mockProcess;
    }

    private ProcessDefinition createProcessDefinition() {
        var activityDefinition = mock(ActivityDefinition.class, withSettings().lenient());
        when(activityDefinition.id()).thenReturn(DEFINITION_ID);
        when(activityDefinition.type()).thenReturn(ActivityType.EXTERNAL_TASK);

        var mockProcessDefinition = mock(ProcessDefinition.class, withSettings().lenient());
        when(mockProcessDefinition.id()).thenReturn("process-definition-id");
        when(mockProcessDefinition.getActivityById(DEFINITION_ID)).thenReturn(Optional.of(activityDefinition));
        return mockProcessDefinition;
    }

    private ActivityExecution createActivityExecution() {
        var process = createProcess();
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.definitionId()).thenReturn(DEFINITION_ID);
        when(mockActivity.process()).thenReturn(process);
        return mockActivity;
    }

}