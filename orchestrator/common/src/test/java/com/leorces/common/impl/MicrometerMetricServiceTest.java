package com.leorces.common.impl;

import com.leorces.common.service.MetricService;
import com.leorces.common.service.impl.MicrometerMetricService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MicrometerMetricService Tests")
class MicrometerMetricServiceTest {

    private static final String COUNTER_NAME = "test.counter";
    private static final String GAUGE_NAME = "test.gauge";
    private static final String HISTOGRAM_NAME = "test.histogram";
    private static final String TIMER_NAME = "test.timer";
    private static final Map<String, String> TEST_LABELS = Map.of("env", "test", "service", "orchestrator");

    private MeterRegistry meterRegistry;
    private MetricService metricService;

    @BeforeEach
    void setUp() {
        // Given
        meterRegistry = new SimpleMeterRegistry();
        metricService = new MicrometerMetricService(meterRegistry);
    }

    @Test
    @DisplayName("Should increment counter by 1 when incrementCounter is called with name only")
    void shouldIncrementCounterByOneWhenIncrementCounterIsCalledWithNameOnly() {
        // When
        metricService.incrementCounter(COUNTER_NAME);

        // Then
        var counter = meterRegistry.find(COUNTER_NAME).counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should increment counter by specified amount when incrementCounter is called with amount")
    void shouldIncrementCounterBySpecifiedAmountWhenIncrementCounterIsCalledWithAmount() {
        // Given
        var amount = 5.5;

        // When
        metricService.incrementCounter(COUNTER_NAME, amount);

        // Then
        var counter = meterRegistry.find(COUNTER_NAME).counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(amount);
    }

    @Test
    @DisplayName("Should increment counter with labels when incrementCounter is called with labels")
    void shouldIncrementCounterWithLabelsWhenIncrementCounterIsCalledWithLabels() {
        // When
        metricService.incrementCounter(COUNTER_NAME, TEST_LABELS);

        // Then
        var counter = findCounterWithTags();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should increment counter by amount with labels when incrementCounter is called with amount and labels")
    void shouldIncrementCounterByAmountWithLabelsWhenIncrementCounterIsCalledWithAmountAndLabels() {
        // Given
        var amount = 3.7;

        // When
        metricService.incrementCounter(COUNTER_NAME, amount, TEST_LABELS);

        // Then
        var counter = findCounterWithTags();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(amount);
    }

    @Test
    @DisplayName("Should set gauge value when setGauge is called")
    void shouldSetGaugeValueWhenSetGaugeIsCalled() {
        // Given
        var value = 42.0;

        // When
        metricService.setGauge(GAUGE_NAME, value);

        // Then
        var gauge = meterRegistry.find(GAUGE_NAME).gauge();
        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(value);
    }

    @Test
    @DisplayName("Should set gauge value with labels when setGauge is called with labels")
    void shouldSetGaugeValueWithLabelsWhenSetGaugeIsCalledWithLabels() {
        // Given
        var value = 24.5;

        // When
        metricService.setGauge(GAUGE_NAME, value, TEST_LABELS);

        // Then
        var gauge = findGaugeWithTags();
        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(value);
    }

    @Test
    @DisplayName("Should increment gauge when incrementGauge is called")
    void shouldIncrementGaugeWhenIncrementGaugeIsCalled() {
        // Given
        var initialValue = 10.0;
        var increment = 5.0;
        metricService.setGauge(GAUGE_NAME, initialValue);

        // When
        metricService.incrementGauge(GAUGE_NAME, increment);

        // Then
        var gauge = meterRegistry.find(GAUGE_NAME).gauge();
        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(initialValue + increment);
    }

    @Test
    @DisplayName("Should create new gauge with increment value when gauge doesn't exist")
    void shouldCreateNewGaugeWithIncrementValueWhenGaugeDoesNotExist() {
        // Given
        var increment = 7.3;

        // When
        metricService.incrementGauge(GAUGE_NAME, increment);

        // Then
        var gauge = meterRegistry.find(GAUGE_NAME).gauge();
        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(increment);
    }

    @Test
    @DisplayName("Should record histogram value when recordHistogram is called")
    void shouldRecordHistogramValueWhenRecordHistogramIsCalled() {
        // Given
        var value = 100.0;

        // When
        metricService.recordHistogram(HISTOGRAM_NAME, value);

        // Then
        var timer = meterRegistry.find(HISTOGRAM_NAME).timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should record histogram value with labels when recordHistogram is called with labels")
    void shouldRecordHistogramValueWithLabelsWhenRecordHistogramIsCalledWithLabels() {
        // Given
        var value = 150.0;

        // When
        metricService.recordHistogram(HISTOGRAM_NAME, value, TEST_LABELS);

        // Then
        var timer = findTimerWithTags(HISTOGRAM_NAME);
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should record timer duration when recordTimer is called")
    void shouldRecordTimerDurationWhenRecordTimerIsCalled() {
        // Given
        var duration = Duration.ofMillis(500);

        // When
        metricService.recordTimer(TIMER_NAME, duration);

        // Then
        var timer = meterRegistry.find(TIMER_NAME).timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should record timer duration with labels when recordTimer is called with labels")
    void shouldRecordTimerDurationWithLabelsWhenRecordTimerIsCalledWithLabels() {
        // Given
        var duration = Duration.ofSeconds(2);

        // When
        metricService.recordTimer(TIMER_NAME, duration, TEST_LABELS);

        // Then
        var timer = findTimerWithTags(TIMER_NAME);
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should start and stop timer sample successfully")
    void shouldStartAndStopTimerSampleSuccessfully() {
        // When
        var timerSample = metricService.startTimer();
        timerSample.stop(TIMER_NAME);

        // Then
        var timer = meterRegistry.find(TIMER_NAME).timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should start and stop timer sample with labels successfully")
    void shouldStartAndStopTimerSampleWithLabelsSuccessfully() {
        // When
        var timerSample = metricService.startTimer();
        timerSample.stop(TIMER_NAME, TEST_LABELS);

        // Then
        var timer = findTimerWithTags(TIMER_NAME);
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    private Counter findCounterWithTags() {
        var meterBuilder = meterRegistry.find(MicrometerMetricServiceTest.COUNTER_NAME);
        MicrometerMetricServiceTest.TEST_LABELS.forEach(meterBuilder::tag);
        return meterBuilder.counter();
    }

    private Gauge findGaugeWithTags() {
        var meterBuilder = meterRegistry.find(MicrometerMetricServiceTest.GAUGE_NAME);
        MicrometerMetricServiceTest.TEST_LABELS.forEach(meterBuilder::tag);
        return meterBuilder.gauge();
    }

    private Timer findTimerWithTags(String name) {
        var meterBuilder = meterRegistry.find(name);
        MicrometerMetricServiceTest.TEST_LABELS.forEach(meterBuilder::tag);
        return meterBuilder.timer();
    }
}