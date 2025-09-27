package com.leorces.engine.activity.behaviour;

import com.leorces.model.runtime.activity.ActivityExecution;

public interface CancellableActivityBehaviour extends ActivityBehavior {

    void cancel(ActivityExecution activity);

    void terminate(ActivityExecution activity);

}
