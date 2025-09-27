package com.leorces.engine.process;

import com.leorces.engine.event.EngineEventBus;
import com.leorces.engine.event.activity.ActivityEvent;
import com.leorces.engine.event.process.complete.CompleteProcessEventAsync;
import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.ActivityPersistence;
import com.leorces.persistence.ProcessPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ProcessCompleteService {

    private final ProcessPersistence processPersistence;
    private final ActivityPersistence activityPersistence;
    private final EngineEventBus eventBus;

    @Async
    @EventListener
    void onApplicationEvent(CompleteProcessEventAsync event) {
        complete(event.process);
    }

    public void complete(Process process) {
        if (!activityPersistence.isAllCompleted(process.id())) {
            return;
        }

        var result = processPersistence.complete(process);
        if (result.isCallActivity()) {
            eventBus.publish(ActivityEvent.completeByIdAsync(process.id()));
        }
    }

}
