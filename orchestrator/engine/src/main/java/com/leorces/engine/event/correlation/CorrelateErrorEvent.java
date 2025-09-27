package com.leorces.engine.event.correlation;


import com.leorces.model.runtime.activity.ActivityExecution;


public class CorrelateErrorEvent extends CorrelationEvent {

    public final ActivityExecution errorEndActivity;

    public CorrelateErrorEvent(ActivityExecution errorEndActivity) {
        super(errorEndActivity.process());
        this.errorEndActivity = errorEndActivity;
    }

}
