package com.leorces.engine.constants;

public class MetricConstants {
    // =====================
    // Base
    // =====================
    public static final String BASE = "leorces.engine";
    // =====================
    // Process
    // =====================
    public static final String PROCESS = BASE + ".process";
    public static final String PROCESS_STARTED = PROCESS + ".started";
    public static final String PROCESS_COMPLETED = PROCESS + ".completed";
    public static final String PROCESS_CANCELLED = PROCESS + ".cancelled";
    public static final String PROCESS_TERMINATED = PROCESS + ".terminated";
    public static final String PROCESS_INCIDENT = PROCESS + ".incident";
    public static final String PROCESS_RECOVERED = PROCESS + ".recovered";
    // =====================
    // Labels
    // =====================
    public static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";

    private MetricConstants() {

    }

}
