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
    private final Map<ActivityType, TriggerableActivityBehaviour> triggerableBehaviors;

    public ActivityBehaviorResolver(List<ActivityBehavior> behaviors,
                                    List<TriggerableActivityBehaviour> triggerableBehaviors) {
        this.behaviors = behaviors.stream()
                .collect(Collectors.toMap(ActivityBehavior::type, Function.identity()));
        this.triggerableBehaviors = triggerableBehaviors.stream()
                .collect(Collectors.toMap(TriggerableActivityBehaviour::type, Function.identity()));
    }

    public Optional<TriggerableActivityBehaviour> resolveTriggerableBehavior(ActivityType activityType) {
        return Optional.ofNullable(triggerableBehaviors.get(activityType));
    }

    public ActivityBehavior resolveBehavior(ActivityType activityType) {
        return behaviors.get(activityType);
    }

}
