package com.leorces.engine.activity.handler;

import com.leorces.api.exception.ExecutionException;
import com.leorces.engine.activity.command.CreateActivityCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;
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
@DisplayName("CreateActivityCommandHandler Tests")
class CreateActivityCommandHandlerTest {

    private static final String DEFINITION_ID = "definition-456";
    private static final String PROCESS_ID = "process-789";

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ActivityDefinition activityDefinition;

    @Mock
    private Process process;

    @Mock
    private ProcessDefinition processDefinition;

    @InjectMocks
    private CreateActivityCommandHandler handler;

    @BeforeEach
    void setUp() {
        when(activityDefinition.id()).thenReturn(DEFINITION_ID);
        when(process.id()).thenReturn(PROCESS_ID);
        when(process.definition()).thenReturn(processDefinition);
    }

    @Test
    @DisplayName("Should create activity execution from definition and process")
    void shouldCreateActivityExecutionFromDefinitionAndProcess() {
        // Given
        var command = CreateActivityCommand.of(activityDefinition, process);

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.definitionId()).isEqualTo(DEFINITION_ID);
        assertThat(result.process()).isEqualTo(process);
    }

    @Test
    @DisplayName("Should create activity by IDs successfully")
    void shouldCreateActivityByIdsSuccessfully() {
        // Given
        var command = CreateActivityCommand.of(DEFINITION_ID, PROCESS_ID);
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(process));
        when(processDefinition.getActivityById(DEFINITION_ID)).thenReturn(Optional.of(activityDefinition));

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.definitionId()).isEqualTo(DEFINITION_ID);
        assertThat(result.process()).isEqualTo(process);
        verify(processPersistence).findById(PROCESS_ID);
        verify(processDefinition).getActivityById(DEFINITION_ID);
    }

    @Test
    @DisplayName("Should throw exception when process not found")
    void shouldThrowExceptionWhenProcessNotFound() {
        // Given
        var command = CreateActivityCommand.of(DEFINITION_ID, PROCESS_ID);
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Process not found");
        verify(processPersistence).findById(PROCESS_ID);
    }

    @Test
    @DisplayName("Should throw exception when definition not found in process")
    void shouldThrowExceptionWhenDefinitionNotFoundInProcess() {
        // Given
        var command = CreateActivityCommand.of(DEFINITION_ID, PROCESS_ID);
        when(processPersistence.findById(PROCESS_ID)).thenReturn(Optional.of(process));
        when(processDefinition.getActivityById(DEFINITION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Activity definition not found");
        verify(processPersistence).findById(PROCESS_ID);
        verify(processDefinition).getActivityById(DEFINITION_ID);
    }

}
