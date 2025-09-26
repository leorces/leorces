package com.leorces.common.service.impl;

import com.leorces.common.service.MetricService;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Map;

/**
 * Decorator implementation of MetricService that can enable or disable metrics collection.
 * When enabled, delegates all calls to the wrapped MetricService implementation.
 * When disabled, all metric operations become no-ops.
 */
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService {

    private final MetricService delegate;
    private final boolean enabled;

    @Override
    public void incrementCounter(String name) {
        if (enabled) {
            delegate.incrementCounter(name);
        }
    }

    @Override
    public void incrementCounter(String name, double amount) {
        if (enabled) {
            delegate.incrementCounter(name, amount);
        }
    }

    @Override
    public void incrementCounter(String name, Map<String, String> labels) {
        if (enabled) {
            delegate.incrementCounter(name, labels);
        }
    }

    @Override
    public void incrementCounter(String name, double amount, Map<String, String> labels) {
        if (enabled) {
            delegate.incrementCounter(name, amount, labels);
        }
    }

    @Override
    public void setGauge(String name, double value) {
        if (enabled) {
            delegate.setGauge(name, value);
        }
    }

    @Override
    public void setGauge(String name, double value, Map<String, String> labels) {
        if (enabled) {
            delegate.setGauge(name, value, labels);
        }
    }

    @Override
    public void incrementGauge(String name, double amount) {
        if (enabled) {
            delegate.incrementGauge(name, amount);
        }
    }

    @Override
    public void incrementGauge(String name, double amount, Map<String, String> labels) {
        if (enabled) {
            delegate.incrementGauge(name, amount, labels);
        }
    }

    @Override
    public void recordHistogram(String name, double value) {
        if (enabled) {
            delegate.recordHistogram(name, value);
        }
    }

    @Override
    public void recordHistogram(String name, double value, Map<String, String> labels) {
        if (enabled) {
            delegate.recordHistogram(name, value, labels);
        }
    }

    @Override
    public void recordTimer(String name, Duration duration) {
        if (enabled) {
            delegate.recordTimer(name, duration);
        }
    }

    @Override
    public void recordTimer(String name, Duration duration, Map<String, String> labels) {
        if (enabled) {
            delegate.recordTimer(name, duration, labels);
        }
    }

    @Override
    public TimerSample startTimer() {
        if (enabled) {
            return delegate.startTimer();
        } else {
            return new NoOpTimerSample();
        }
    }

    /**
     * No-op implementation of TimerSample used when metrics are disabled.
     */
    private static class NoOpTimerSample implements TimerSample {

        @Override
        public void stop(String name) {
            // No-op when metrics are disabled
        }

        @Override
        public void stop(String name, Map<String, String> labels) {
            // No-op when metrics are disabled
        }

    }

}