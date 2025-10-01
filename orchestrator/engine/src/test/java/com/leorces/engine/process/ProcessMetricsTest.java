package com.leorces.engine.process;

import com.leorces.common.service.MetricService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.runtime.process.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static com.leorces.engine.constants.MetricConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessMetricsTest Tests")
class ProcessMetricsTest {

    @Mock
    private MetricService metricService;

    @Mock
    private ProcessDefinition definition;

    private ProcessMetrics processMetrics;

    private Process process;

    @BeforeEach
    void setUp() {
        processMetrics = new ProcessMetrics(metricService);

        process = Process.builder()
                .id("p1")
                .businessKey("bk1")
                .definition(definition)
                .state(null)
                .variables(List.of())
                .build();

        lenient().when(definition.key()).thenReturn("order-process");
    }

    @Test
    @DisplayName("recordProcessStartedMetric should call incrementCounter with correct labels")
    void recordProcessStartedMetricShouldCallIncrementCounterWithCorrectLabels() {
        processMetrics.recordProcessStartedMetric(process);
        verifyMetricCalled(PROCESS_STARTED);
    }

    @Test
    @DisplayName("recordProcessCompletedMetric should call incrementCounter with correct labels")
    void recordProcessCompletedMetricShouldCallIncrementCounterWithCorrectLabels() {
        processMetrics.recordProcessCompletedMetric(process);
        verifyMetricCalled(PROCESS_COMPLETED);
    }

    @Test
    @DisplayName("recordProcessCancelledMetric should call incrementCounter with correct labels")
    void recordProcessCancelledMetricShouldCallIncrementCounterWithCorrectLabels() {
        processMetrics.recordProcessCancelledMetric(process);
        verifyMetricCalled(PROCESS_CANCELLED);
    }

    @Test
    @DisplayName("recordProcessTerminatedMetric should call incrementCounter with correct labels")
    void recordProcessTerminatedMetricShouldCallIncrementCounterWithCorrectLabels() {
        processMetrics.recordProcessTerminatedMetric(process);
        verifyMetricCalled(PROCESS_TERMINATED);
    }

    @Test
    @DisplayName("recordProcessIncidentMetric should call incrementCounter with correct labels")
    void recordProcessIncidentMetricShouldCallIncrementCounterWithCorrectLabels() {
        processMetrics.recordProcessIncidentMetric(process);
        verifyMetricCalled(PROCESS_INCIDENT);
    }

    @Test
    @DisplayName("recordProcessRecoveredMetrics should call incrementCounter with correct labels")
    void recordProcessRecoveredMetricsShouldCallIncrementCounterWithCorrectLabels() {
        processMetrics.recordProcessRecoveredMetrics(process);
        verifyMetricCalled(PROCESS_RECOVERED);
    }

    private void verifyMetricCalled(String metricName) {
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(metricService).incrementCounter(eq(metricName), captor.capture());
        assertThat(captor.getValue()).containsEntry(PROCESS_DEFINITION_KEY, "order-process");
    }

}
