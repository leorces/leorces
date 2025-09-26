package com.leorces.common.service;

import java.time.Duration;
import java.util.Map;

/**
 * Service for managing Prometheus metrics.
 * Provides functionality to register and update various metric types including counters, gauges, histograms, and timers.
 */
public interface MetricService {

    /**
     * Increments a counter metric by 1.
     *
     * @param name the name of the counter metric
     */
    void incrementCounter(String name);

    /**
     * Increments a counter metric by the specified amount.
     *
     * @param name   the name of the counter metric
     * @param amount the amount to increment by
     */
    void incrementCounter(String name, double amount);

    /**
     * Increments a counter metric by 1 with labels.
     *
     * @param name   the name of the counter metric
     * @param labels the labels to associate with the metric
     */
    void incrementCounter(String name, Map<String, String> labels);

    /**
     * Increments a counter metric by the specified amount with labels.
     *
     * @param name   the name of the counter metric
     * @param amount the amount to increment by
     * @param labels the labels to associate with the metric
     */
    void incrementCounter(String name, double amount, Map<String, String> labels);

    /**
     * Sets the value of a gauge metric.
     *
     * @param name  the name of the gauge metric
     * @param value the value to set
     */
    void setGauge(String name, double value);

    /**
     * Sets the value of a gauge metric with labels.
     *
     * @param name   the name of the gauge metric
     * @param value  the value to set
     * @param labels the labels to associate with the metric
     */
    void setGauge(String name, double value, Map<String, String> labels);

    /**
     * Increments a gauge metric by the specified amount.
     *
     * @param name   the name of the gauge metric
     * @param amount the amount to increment by
     */
    void incrementGauge(String name, double amount);

    /**
     * Increments a gauge metric by the specified amount with labels.
     *
     * @param name   the name of the gauge metric
     * @param amount the amount to increment by
     * @param labels the labels to associate with the metric
     */
    void incrementGauge(String name, double amount, Map<String, String> labels);

    /**
     * Records an observation for a histogram metric.
     *
     * @param name  the name of the histogram metric
     * @param value the value to observe
     */
    void recordHistogram(String name, double value);

    /**
     * Records an observation for a histogram metric with labels.
     *
     * @param name   the name of the histogram metric
     * @param value  the value to observe
     * @param labels the labels to associate with the metric
     */
    void recordHistogram(String name, double value, Map<String, String> labels);

    /**
     * Records a timing observation for a timer metric.
     *
     * @param name     the name of the timer metric
     * @param duration the duration to record
     */
    void recordTimer(String name, Duration duration);

    /**
     * Records a timing observation for a timer metric with labels.
     *
     * @param name     the name of the timer metric
     * @param duration the duration to record
     * @param labels   the labels to associate with the metric
     */
    void recordTimer(String name, Duration duration, Map<String, String> labels);

    /**
     * Starts a timer and returns a timer sample that can be stopped to record the duration.
     *
     * @return a timer sample
     */
    TimerSample startTimer();

    /**
     * Timer sample interface for measuring elapsed time.
     */
    interface TimerSample {

        /**
         * Stops the timer and records the elapsed time for the specified metric.
         *
         * @param name the name of the timer metric
         */
        void stop(String name);

        /**
         * Stops the timer and records the elapsed time for the specified metric with labels.
         *
         * @param name   the name of the timer metric
         * @param labels the labels to associate with the metric
         */
        void stop(String name, Map<String, String> labels);

    }

}