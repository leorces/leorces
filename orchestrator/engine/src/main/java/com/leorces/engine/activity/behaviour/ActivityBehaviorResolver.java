package com.leorces.engine.activity.behaviour;

import com.leorces.model.definition.activity.ActivityType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ActivityBehaviorResolver {

    private final Map<ActivityType, ActivityBehavior> behaviors;
    private final Map<ActivityType, FailableActivityBehavior> failableBehaviors;
    private final Map<ActivityType, TriggerableActivityBehaviour> triggerableBehaviors;
    private final Map<ActivityType, CancellableActivityBehaviour> cancellableBehaviors;

    public ActivityBehaviorResolver(List<ActivityBehavior> behaviors,
                                    List<FailableActivityBehavior> failableBehaviors,
                                    List<TriggerableActivityBehaviour> triggerableBehaviors,
                                    List<CancellableActivityBehaviour> cancellableBehaviors) {
        this.behaviors = behaviors.stream()
                .collect(Collectors.toMap(ActivityBehavior::type, Function.identity()));
        this.failableBehaviors = failableBehaviors.stream()
                .collect(Collectors.toMap(FailableActivityBehavior::type, Function.identity()));
        this.triggerableBehaviors = triggerableBehaviors.stream()
                .collect(Collectors.toMap(TriggerableActivityBehaviour::type, Function.identity()));
        this.cancellableBehaviors = cancellableBehaviors.stream()
                .collect(Collectors.toMap(CancellableActivityBehaviour::type, Function.identity()));
    }

    public Optional<TriggerableActivityBehaviour> resolveTriggerableStrategy(ActivityType activityType) {
        return Optional.ofNullable(triggerableBehaviors.get(activityType));
    }

    public Optional<FailableActivityBehavior> resolveFailableStrategy(ActivityType activityType) {
        return Optional.ofNullable(failableBehaviors.get(activityType));
    }

    public Optional<CancellableActivityBehaviour> resolveCancellableStrategy(ActivityType activityType) {
        return Optional.ofNullable(cancellableBehaviors.get(activityType));
    }

    public ActivityBehavior resolveStrategy(ActivityType activityType) {
        return behaviors.get(activityType);
    }

}
