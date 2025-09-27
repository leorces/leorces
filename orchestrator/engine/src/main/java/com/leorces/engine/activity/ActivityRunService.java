package com.leorces.engine.activity;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.event.activity.run.RunActivitiesEventAsync;
import com.leorces.engine.event.activity.run.RunActivityByDefinitionEventAsync;
import com.leorces.engine.event.activity.run.RunActivityByDefinitionIdAsync;
import com.leorces.engine.event.activity.run.RunActivityEventAsync;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.engine.variables.VariableRuntimeService;
import com.leorces.model.runtime.activity.ActivityExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class ActivityRunService {

    private final VariableRuntimeService variableRuntimeService;
    private final ActivityBehaviorResolver behaviorResolver;
    private final ActivityFactory activityFactory;
    private final TaskExecutorService taskService;

    @Async
    @EventListener
    void handleRun(RunActivityEventAsync event) {
        run(event.activity);
    }

    @Async
    @EventListener
    void handleRun(RunActivityByDefinitionEventAsync event) {
        run(activityFactory.createActivity(event.definition, event.process));
    }

    @Async
    @EventListener
    void handleRun(RunActivityByDefinitionIdAsync event) {
        run(activityFactory.getNewByDefinitionId(event.definitionId, event.processId));
    }

    @Async
    @EventListener
    void handleRun(RunActivitiesEventAsync event) {
        event.definitions.stream()
                .map(definition -> activityFactory.createActivity(definition, event.process))
                .forEach(activity -> taskService.execute(() -> run(activity)));
    }

    private void run(ActivityExecution activity) {
        log.debug("Run {} activity with definitionId: {} and processId: {}", activity.type(), activity.definitionId(), activity.processId());
        var activityToRun = processInputVariables(activity);
        behaviorResolver.resolveStrategy(activityToRun.type()).run(activityToRun);
    }

    private ActivityExecution processInputVariables(ActivityExecution activity) {
        var variables = variableRuntimeService.evaluate(activity, activity.inputs());
        return activity.toBuilder().variables(variables).build();
    }

}
