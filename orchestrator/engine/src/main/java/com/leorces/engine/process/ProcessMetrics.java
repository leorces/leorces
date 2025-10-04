package com.leorces.engine.process;

import com.leorces.common.service.MetricService;
import com.leorces.model.runtime.process.Process;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

import static com.leorces.engine.constants.MetricConstants.*;

@Component
@RequiredArgsConstructor
public class ProcessMetrics {

    private final MetricService metricService;

    public void recordProcessStartedMetric(Process process) {
        withLabels(process, labels ->
                metricService.incrementCounter(PROCESS_STARTED, labels)
        );
    }

    public void recordProcessCompletedMetric(Process process) {
        withLabels(process, labels ->
                metricService.incrementCounter(PROCESS_COMPLETED, labels)
        );
    }

    public void recordProcessTerminatedMetric(Process process) {
        withLabels(process, labels ->
                metricService.incrementCounter(PROCESS_TERMINATED, labels)
        );
    }

    public void recordProcessIncidentMetric(Process process) {
        withLabels(process, labels ->
                metricService.incrementCounter(PROCESS_INCIDENT, labels)
        );
    }

    public void recordProcessRecoveredMetrics(Process process) {
        withLabels(process, labels ->
                metricService.incrementCounter(PROCESS_RECOVERED, labels)
        );
    }

    private void withLabels(Process process, Consumer<Map<String, String>> metricsConsumer) {
        var labels = createLabels(process.definition().key());
        metricsConsumer.accept(labels);
    }

    private Map<String, String> createLabels(String processDefinitionKey) {
        return Map.of(PROCESS_DEFINITION_KEY, processDefinitionKey);
    }

}
