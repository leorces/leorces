package com.leorces.common.impl;

import com.leorces.common.service.MetricService;
import com.leorces.common.service.impl.MetricServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MetricServiceImpl Tests")
class MetricServiceImplTest {

    private static final String METRIC_NAME = "test.metric";
    private static final Map<String, String> TEST_LABELS = Map.of("env", "test");
    private static final double VALUE = 42.0;
    private static final Duration DURATION = Duration.ofMillis(100);

    @Mock
    private MetricService delegate;

    @Mock
    private MetricService.TimerSample delegateTimerSample;

    private MetricService enabledService;
    private MetricService disabledService;

    @BeforeEach
    void setUp() {
        enabledService = new MetricServiceImpl(delegate, true);
        disabledService = new MetricServiceImpl(delegate, false);
    }

    @Test
    @DisplayName("Should delegate incrementCounter when enabled")
    void shouldDelegateIncrementCounterWhenEnabled() {
        // When
        enabledService.incrementCounter(METRIC_NAME);

        // Then
        verify(delegate).incrementCounter(METRIC_NAME);
    }

    @Test
    @DisplayName("Should not delegate incrementCounter when disabled")
    void shouldNotDelegateIncrementCounterWhenDisabled() {
        // When
        disabledService.incrementCounter(METRIC_NAME);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate incrementCounter with amount when enabled")
    void shouldDelegateIncrementCounterWithAmountWhenEnabled() {
        // When
        enabledService.incrementCounter(METRIC_NAME, VALUE);

        // Then
        verify(delegate).incrementCounter(METRIC_NAME, VALUE);
    }

    @Test
    @DisplayName("Should not delegate incrementCounter with amount when disabled")
    void shouldNotDelegateIncrementCounterWithAmountWhenDisabled() {
        // When
        disabledService.incrementCounter(METRIC_NAME, VALUE);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate incrementCounter with labels when enabled")
    void shouldDelegateIncrementCounterWithLabelsWhenEnabled() {
        // When
        enabledService.incrementCounter(METRIC_NAME, TEST_LABELS);

        // Then
        verify(delegate).incrementCounter(METRIC_NAME, TEST_LABELS);
    }

    @Test
    @DisplayName("Should not delegate incrementCounter with labels when disabled")
    void shouldNotDelegateIncrementCounterWithLabelsWhenDisabled() {
        // When
        disabledService.incrementCounter(METRIC_NAME, TEST_LABELS);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate incrementCounter with amount and labels when enabled")
    void shouldDelegateIncrementCounterWithAmountAndLabelsWhenEnabled() {
        // When
        enabledService.incrementCounter(METRIC_NAME, VALUE, TEST_LABELS);

        // Then
        verify(delegate).incrementCounter(METRIC_NAME, VALUE, TEST_LABELS);
    }

    @Test
    @DisplayName("Should not delegate incrementCounter with amount and labels when disabled")
    void shouldNotDelegateIncrementCounterWithAmountAndLabelsWhenDisabled() {
        // When
        disabledService.incrementCounter(METRIC_NAME, VALUE, TEST_LABELS);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate setGauge when enabled")
    void shouldDelegateSetGaugeWhenEnabled() {
        // When
        enabledService.setGauge(METRIC_NAME, VALUE);

        // Then
        verify(delegate).setGauge(METRIC_NAME, VALUE);
    }

    @Test
    @DisplayName("Should not delegate setGauge when disabled")
    void shouldNotDelegateSetGaugeWhenDisabled() {
        // When
        disabledService.setGauge(METRIC_NAME, VALUE);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate setGauge with labels when enabled")
    void shouldDelegateSetGaugeWithLabelsWhenEnabled() {
        // When
        enabledService.setGauge(METRIC_NAME, VALUE, TEST_LABELS);

        // Then
        verify(delegate).setGauge(METRIC_NAME, VALUE, TEST_LABELS);
    }

    @Test
    @DisplayName("Should not delegate setGauge with labels when disabled")
    void shouldNotDelegateSetGaugeWithLabelsWhenDisabled() {
        // When
        disabledService.setGauge(METRIC_NAME, VALUE, TEST_LABELS);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate incrementGauge when enabled")
    void shouldDelegateIncrementGaugeWhenEnabled() {
        // When
        enabledService.incrementGauge(METRIC_NAME, VALUE);

        // Then
        verify(delegate).incrementGauge(METRIC_NAME, VALUE);
    }

    @Test
    @DisplayName("Should not delegate incrementGauge when disabled")
    void shouldNotDelegateIncrementGaugeWhenDisabled() {
        // When
        disabledService.incrementGauge(METRIC_NAME, VALUE);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate incrementGauge with labels when enabled")
    void shouldDelegateIncrementGaugeWithLabelsWhenEnabled() {
        // When
        enabledService.incrementGauge(METRIC_NAME, VALUE, TEST_LABELS);

        // Then
        verify(delegate).incrementGauge(METRIC_NAME, VALUE, TEST_LABELS);
    }

    @Test
    @DisplayName("Should not delegate incrementGauge with labels when disabled")
    void shouldNotDelegateIncrementGaugeWithLabelsWhenDisabled() {
        // When
        disabledService.incrementGauge(METRIC_NAME, VALUE, TEST_LABELS);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate recordHistogram when enabled")
    void shouldDelegateRecordHistogramWhenEnabled() {
        // When
        enabledService.recordHistogram(METRIC_NAME, VALUE);

        // Then
        verify(delegate).recordHistogram(METRIC_NAME, VALUE);
    }

    @Test
    @DisplayName("Should not delegate recordHistogram when disabled")
    void shouldNotDelegateRecordHistogramWhenDisabled() {
        // When
        disabledService.recordHistogram(METRIC_NAME, VALUE);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate recordHistogram with labels when enabled")
    void shouldDelegateRecordHistogramWithLabelsWhenEnabled() {
        // When
        enabledService.recordHistogram(METRIC_NAME, VALUE, TEST_LABELS);

        // Then
        verify(delegate).recordHistogram(METRIC_NAME, VALUE, TEST_LABELS);
    }

    @Test
    @DisplayName("Should not delegate recordHistogram with labels when disabled")
    void shouldNotDelegateRecordHistogramWithLabelsWhenDisabled() {
        // When
        disabledService.recordHistogram(METRIC_NAME, VALUE, TEST_LABELS);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate recordTimer when enabled")
    void shouldDelegateRecordTimerWhenEnabled() {
        // When
        enabledService.recordTimer(METRIC_NAME, DURATION);

        // Then
        verify(delegate).recordTimer(METRIC_NAME, DURATION);
    }

    @Test
    @DisplayName("Should not delegate recordTimer when disabled")
    void shouldNotDelegateRecordTimerWhenDisabled() {
        // When
        disabledService.recordTimer(METRIC_NAME, DURATION);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate recordTimer with labels when enabled")
    void shouldDelegateRecordTimerWithLabelsWhenEnabled() {
        // When
        enabledService.recordTimer(METRIC_NAME, DURATION, TEST_LABELS);

        // Then
        verify(delegate).recordTimer(METRIC_NAME, DURATION, TEST_LABELS);
    }

    @Test
    @DisplayName("Should not delegate recordTimer with labels when disabled")
    void shouldNotDelegateRecordTimerWithLabelsWhenDisabled() {
        // When
        disabledService.recordTimer(METRIC_NAME, DURATION, TEST_LABELS);

        // Then
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Should delegate startTimer when enabled")
    void shouldDelegateStartTimerWhenEnabled() {
        // Given
        when(delegate.startTimer()).thenReturn(delegateTimerSample);

        // When
        var timerSample = enabledService.startTimer();

        // Then
        verify(delegate).startTimer();
        assertThat(timerSample).isSameAs(delegateTimerSample);
    }

    @Test
    @DisplayName("Should return NoOpTimerSample when disabled")
    void shouldReturnNoOpTimerSampleWhenDisabled() {
        // When
        var timerSample = disabledService.startTimer();

        // Then
        verifyNoInteractions(delegate);
        assertThat(timerSample).isNotNull();
        assertThat(timerSample).isNotSameAs(delegateTimerSample);
    }

    @Test
    @DisplayName("Should handle NoOpTimerSample stop method without errors")
    void shouldHandleNoOpTimerSampleStopMethodWithoutErrors() {
        // Given
        var timerSample = disabledService.startTimer();

        // When & Then - should not throw any exceptions
        timerSample.stop(METRIC_NAME);
        timerSample.stop(METRIC_NAME, TEST_LABELS);
    }
}