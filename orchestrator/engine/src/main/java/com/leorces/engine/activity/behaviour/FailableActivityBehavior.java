package com.leorces.engine.activity.behaviour;

import com.leorces.model.runtime.activity.ActivityExecution;

public interface FailableActivityBehavior extends ActivityBehavior {

    void fail(ActivityExecution activity);

    void retry(ActivityExecution activity);

}
