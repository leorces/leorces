package com.leorces.engine.process;

import com.leorces.engine.activity.command.RunActivityCommand;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.engine.exception.activity.ActivityNotFoundException;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.persistence.ProcessPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessRuntimeServiceTest Tests")
class ProcessRuntimeServiceTest {

    @Mock
    private ProcessDefinition definition;

    @Mock
    private ActivityDefinition startActivity;

    @Mock
    private ProcessFactory processFactory;

    @Mock
    private ProcessPersistence processPersistence;

    @Mock
    private ProcessMetrics processMetrics;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private ProcessRuntimeService runtimeService;

    private Process process;

    @BeforeEach
    void setUp() {
        lenient().when(definition.key()).thenReturn("order-process");
        lenient().when(definition.id()).thenReturn("def-1");
        lenient().when(definition.getStartActivity()).thenReturn(Optional.of(startActivity));

        process = Process.builder()
                .id("p1")
                .businessKey("bk1")
                .definition(definition)
                .state(ProcessState.ACTIVE)
                .variables(List.of())
                .build();
    }

    @Test
    @DisplayName("startByDefinitionId should create process, run it, record metric and dispatch start activity")
    void startByDefinitionIdShouldCreateRunRecordMetricAndDispatch() {
        when(processFactory.createByDefinitionId("def-1", "bk1", Map.of())).thenReturn(process);
        when(processPersistence.run(process)).thenReturn(process);

        runtimeService.startByDefinitionId("def-1", "bk1", Map.of());

        verify(processFactory).createByDefinitionId("def-1", "bk1", Map.of());
        verify(processPersistence).run(process);
        verify(processMetrics).recordProcessStartedMetric(process);
        verify(dispatcher).dispatch(any(RunActivityCommand.class));
    }

    @Test
    @DisplayName("startByDefinitionKey should create process, run it, record metric and dispatch start activity")
    void startByDefinitionKeyShouldCreateRunRecordMetricAndDispatch() {
        when(processFactory.createByDefinitionKey("order-process", "bk1", Map.of())).thenReturn(process);
        when(processPersistence.run(process)).thenReturn(process);

        runtimeService.startByDefinitionKey("order-process", "bk1", Map.of());

        verify(processFactory).createByDefinitionKey("order-process", "bk1", Map.of());
        verify(processPersistence).run(process);
        verify(processMetrics).recordProcessStartedMetric(process);
        verify(dispatcher).dispatch(any(RunActivityCommand.class));
    }

    @Test
    @DisplayName("start should throw ActivityNotFoundException when start activity is missing")
    void startShouldThrowWhenNoStartActivity() {
        when(definition.getStartActivity()).thenReturn(Optional.empty());
        when(processPersistence.run(process)).thenReturn(process);

        assertThatThrownBy(() -> runtimeService.start(process))
                .isInstanceOf(ActivityNotFoundException.class);

        verify(dispatcher, never()).dispatch(any());
    }

    @Test
    @DisplayName("start should dispatch RunActivityCommand with correct process and startActivity")
    void startShouldDispatchRunActivityCommandWithCorrectArguments() {
        when(processPersistence.run(process)).thenReturn(process);

        runtimeService.start(process);

        ArgumentCaptor<RunActivityCommand> captor = ArgumentCaptor.forClass(RunActivityCommand.class);
        verify(dispatcher).dispatch(captor.capture());

        RunActivityCommand command = captor.getValue();
        assertThat(command.process()).isEqualTo(process);
    }

    @Test
    @DisplayName("start should record process started metric")
    void startShouldRecordProcessStartedMetric() {
        when(processPersistence.run(process)).thenReturn(process);

        runtimeService.start(process);

        verify(processMetrics).recordProcessStartedMetric(process);
    }

}
