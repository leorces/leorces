package com.leorces.rest.client.model.worker;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class WorkerState {

    public final int maxCapacity;
    public final AtomicInteger activeTasks = new AtomicInteger(0);
    public final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    public final AtomicLong currentBackoffInterval = new AtomicLong(0);
    final AtomicLong lastSuccessfulPoll = new AtomicLong(System.currentTimeMillis());
    final AtomicLong lastPollTime = new AtomicLong(0);

    public WorkerState(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void recordSuccessfulPoll() {
        consecutiveFailures.set(0);
        currentBackoffInterval.set(0);
        lastSuccessfulPoll.set(System.currentTimeMillis());
        lastPollTime.set(System.currentTimeMillis());
    }

    public void recordFailedPoll(long baseInterval, double backoffMultiplier, long maxBackoffInterval) {
        consecutiveFailures.incrementAndGet();
        var currentInterval = currentBackoffInterval.get();
        if (currentInterval == 0) {
            currentInterval = baseInterval;
        } else {
            currentInterval = Math.min((long) (currentInterval * backoffMultiplier), maxBackoffInterval);
        }
        currentBackoffInterval.set(currentInterval);
        lastPollTime.set(System.currentTimeMillis());
    }

    public boolean shouldPoll(long baseInterval) {
        var currentTime = System.currentTimeMillis();
        var timeSinceLastPoll = currentTime - lastPollTime.get();
        var requiredInterval = currentBackoffInterval.get() > 0 ? currentBackoffInterval.get() : baseInterval;
        return timeSinceLastPoll >= requiredInterval;
    }

}
