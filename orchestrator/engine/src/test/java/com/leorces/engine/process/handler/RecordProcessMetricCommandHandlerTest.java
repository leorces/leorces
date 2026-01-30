package com.leorces.engine.process.handler;

import com.leorces.common.service.MetricService;
import com.leorces.engine.process.command.RecordProcessMetricCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.process.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.leorces.engine.constants.MetricConstants.PROCESS_DEFINITION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordProcessMetricCommandHandler Tests")
class RecordProcessMetricCommandHandlerTest {

    private static final String METRIC_NAME = "test-metric";
    private static final String DEFINITION_KEY = "order-process";

    @Mock
    private MetricService metricService;

    @Mock
    private ProcessDefinition definition;

    @InjectMocks
    private RecordProcessMetricCommandHandler handler;

    private Process process;

    @BeforeEach
    void setUp() {
        process = Process.builder()
                .definition(definition)
                .build();

        when(definition.key()).thenReturn(DEFINITION_KEY);
    }

    @Test
    @DisplayName("handle should call incrementCounter with correct labels")
    void handleShouldCallIncrementCounterWithCorrectLabels() {
        // Given
        var command = RecordProcessMetricCommand.of(METRIC_NAME, process);

        // When
        handler.handle(command);

        // Then
        var captor = ArgumentCaptor.forClass(Map.class);
        verify(metricService).incrementCounter(eq(METRIC_NAME), captor.capture());
        assertThat(captor.getValue()).containsEntry(PROCESS_DEFINITION_KEY, DEFINITION_KEY);
    }

}
