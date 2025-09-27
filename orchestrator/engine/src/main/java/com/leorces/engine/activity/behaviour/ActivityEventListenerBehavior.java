package com.leorces.engine.activity.behaviour;

import com.leorces.engine.event.activity.ActivityEvent;

public interface ActivityEventListenerBehavior {

    default void handle(ActivityEvent event) {

    }

}
