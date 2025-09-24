package com.leorces.common.service.impl;

import com.leorces.common.service.MetricService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of MetricService using Micrometer for Prometheus metrics.
 */
@RequiredArgsConstructor
public class MicrometerMetricService implements MetricService {

    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicReference<Double>> gaugeReferences = new ConcurrentHashMap<>();

    @Override
    public void incrementCounter(String name) {
        Counter.builder(name)
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void incrementCounter(String name, double amount) {
        Counter.builder(name)
                .register(meterRegistry)
                .increment(amount);
    }

    @Override
    public void incrementCounter(String name, Map<String, String> labels) {
        buildCounterWithTags(name, labels).increment();
    }

    @Override
    public void incrementCounter(String name, double amount, Map<String, String> labels) {
        buildCounterWithTags(name, labels).increment(amount);
    }

    @Override
    public void setGauge(String name, double value) {
        var gaugeKey = buildGaugeKey(name, Map.of());
        var atomicValue = gaugeReferences.computeIfAbsent(gaugeKey, k -> new AtomicReference<>(value));
        atomicValue.set(value);

        if (meterRegistry.find(name).gauge() == null) {
            Gauge.builder(name, atomicValue, AtomicReference::get)
                    .register(meterRegistry);
        }
    }

    @Override
    public void setGauge(String name, double value, Map<String, String> labels) {
        var gaugeKey = buildGaugeKey(name, labels);
        var atomicValue = gaugeReferences.computeIfAbsent(gaugeKey, k -> new AtomicReference<>(value));
        atomicValue.set(value);

        if (findGaugeWithTags(name, labels) == null) {
            var builder = Gauge.builder(name, atomicValue, AtomicReference::get);
            labels.forEach(builder::tag);
            builder.register(meterRegistry);
        }
    }

    @Override
    public void incrementGauge(String name, double amount) {
        var gaugeKey = buildGaugeKey(name, Map.of());
        var atomicValue = gaugeReferences.get(gaugeKey);
        if (atomicValue != null) {
            atomicValue.updateAndGet(current -> current + amount);
        } else {
            setGauge(name, amount);
        }
    }

    @Override
    public void incrementGauge(String name, double amount, Map<String, String> labels) {
        var gaugeKey = buildGaugeKey(name, labels);
        var atomicValue = gaugeReferences.get(gaugeKey);
        if (atomicValue != null) {
            atomicValue.updateAndGet(current -> current + amount);
        } else {
            setGauge(name, amount, labels);
        }
    }

    @Override
    public void recordHistogram(String name, double value) {
        Timer.builder(name)
                .register(meterRegistry)
                .record(Duration.ofMillis((long) value));
    }

    @Override
    public void recordHistogram(String name, double value, Map<String, String> labels) {
        buildTimerWithTags(name, labels).record(Duration.ofMillis((long) value));
    }

    @Override
    public void recordTimer(String name, Duration duration) {
        Timer.builder(name)
                .register(meterRegistry)
                .record(duration);
    }

    @Override
    public void recordTimer(String name, Duration duration, Map<String, String> labels) {
        buildTimerWithTags(name, labels).record(duration);
    }

    @Override
    public TimerSample startTimer() {
        var sample = Timer.start(meterRegistry);
        return new MicrometerTimerSample(sample);
    }

    private Counter buildCounterWithTags(String name, Map<String, String> labels) {
        var builder = Counter.builder(name);
        labels.forEach(builder::tag);
        return builder.register(meterRegistry);
    }

    private Timer buildTimerWithTags(String name, Map<String, String> labels) {
        var builder = Timer.builder(name);
        labels.forEach(builder::tag);
        return builder.register(meterRegistry);
    }

    private Gauge findGaugeWithTags(String name, Map<String, String> labels) {
        var meterBuilder = meterRegistry.find(name);
        labels.forEach(meterBuilder::tag);
        return meterBuilder.gauge();
    }

    private String buildGaugeKey(String name, Map<String, String> labels) {
        if (labels.isEmpty()) {
            return name;
        }
        var keyBuilder = new StringBuilder(name);
        labels.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> keyBuilder.append(":")
                        .append(entry.getKey())
                        .append("=")
                        .append(entry.getValue()));
        return keyBuilder.toString();
    }

    /**
     * Implementation of TimerSample using Micrometer Timer.Sample.
     */
    private class MicrometerTimerSample implements TimerSample {

        private final Timer.Sample sample;

        private MicrometerTimerSample(Timer.Sample sample) {
            this.sample = sample;
        }

        @Override
        public void stop(String name) {
            sample.stop(Timer.builder(name).register(meterRegistry));
        }

        @Override
        public void stop(String name, Map<String, String> labels) {
            var builder = Timer.builder(name);
            labels.forEach(builder::tag);
            sample.stop(builder.register(meterRegistry));
        }
    }
}