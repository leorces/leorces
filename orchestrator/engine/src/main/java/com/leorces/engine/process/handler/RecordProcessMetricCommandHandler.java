package com.leorces.engine.process.handler;

import com.leorces.common.service.MetricService;
import com.leorces.engine.core.CommandHandler;
import com.leorces.engine.process.command.RecordProcessMetricCommand;
import com.leorces.model.runtime.process.Process;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

import static com.leorces.engine.constants.MetricConstants.PROCESS_DEFINITION_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordProcessMetricCommandHandler implements CommandHandler<RecordProcessMetricCommand> {

    private final MetricService metricService;

    @Override
    public void handle(RecordProcessMetricCommand command) {
        var metricName = command.metricName();
        var process = command.process();
        withLabels(process, labels ->
                metricService.incrementCounter(metricName, labels)
        );
    }

    @Override
    public Class<RecordProcessMetricCommand> getCommandType() {
        return RecordProcessMetricCommand.class;
    }

    private void withLabels(Process process, Consumer<Map<String, String>> metricsConsumer) {
        var labels = createLabels(process.definition().key());
        metricsConsumer.accept(labels);
    }

    private Map<String, String> createLabels(String processDefinitionKey) {
        return Map.of(PROCESS_DEFINITION_KEY, processDefinitionKey);
    }

}
