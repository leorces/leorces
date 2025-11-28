package com.leorces.rest.client.constants;

public final class ApiConstants {
    // =====================
    // Base
    // =====================
    public static final String API_BASE_PATH = "/api/v1";
    public static final String SIZE_PARAM = "limit";
    // =====================
    // Runtime endpoints
    // =====================
    public static final String RUNTIME_ENDPOINT = API_BASE_PATH + "/runtime";
    public static final String START_PROCESS_BY_KEY_ENDPOINT = RUNTIME_ENDPOINT + "/processes/key";
    public static final String START_PROCESS_BY_ID_ENDPOINT = RUNTIME_ENDPOINT + "/processes";
    public static final String TERMINATE_PROCESS_BY_ID_ENDPOINT = RUNTIME_ENDPOINT + "/processes/%s/terminate";
    public static final String RESOLVE_INCIDENT_BY_PROCESS_ID = RUNTIME_ENDPOINT + "/processes/%s/resolve-incident";
    public static final String MODIFY_PROCESS_BY_ID_ENDPOINT = RUNTIME_ENDPOINT + "/processes/%s/modification";
    public static final String CORRELATE_MESSAGE_ENDPOINT = RUNTIME_ENDPOINT + "/correlate";
    public static final String SET_VARIABLES_ENDPOINT = RUNTIME_ENDPOINT + "/%s/variables";
    public static final String SET_VARIABLES_LOCAL_ENDPOINT = RUNTIME_ENDPOINT + "/%s/variables/local";
    public static final String FIND_PROCESS = RUNTIME_ENDPOINT + "/find/process";
    // =====================
    // Activities endpoints
    // =====================
    public static final String ACTIVITIES_ENDPOINT = API_BASE_PATH + "/activities";
    public static final String POLL_ACTIVITIES_ENDPOINT = ACTIVITIES_ENDPOINT + "/poll/%s/%s";
    public static final String COMPLETE_ACTIVITY_ENDPOINT = ACTIVITIES_ENDPOINT + "/%s/complete";
    public static final String FAIL_ACTIVITY_ENDPOINT = ACTIVITIES_ENDPOINT + "/%s/fail";
    public static final String RUN_ACTIVITY_ENDPOINT = ACTIVITIES_ENDPOINT + "/%s/%s/run";
    public static final String TERMINATE_ACTIVITY_ENDPOINT = ACTIVITIES_ENDPOINT + "/%s/terminate";
    public static final String RETRY_ACTIVITY_ENDPOINT = ACTIVITIES_ENDPOINT + "/%s/retry";
    // =====================
    // Process endpoints
    // =====================
    public static final String PROCESSES_ENDPOINT = API_BASE_PATH + "/processes";
    public static final String PROCESS_BY_ID_ENDPOINT = PROCESSES_ENDPOINT + "/%s";
    // =====================
    // Definition endpoints
    // =====================
    public static final String DEFINITIONS_ENDPOINT = API_BASE_PATH + "/definitions";
    public static final String DEFINITION_BY_ID_ENDPOINT = DEFINITIONS_ENDPOINT + "/%s";
    // =====================
    // History endpoints
    // =====================
    public static final String HISTORY_ENDPOINT = API_BASE_PATH + "/history";
    // =====================
    // Repository endpoints
    // =====================
    public static final String ADMIN_ENDPOINT = API_BASE_PATH + "/admin";
    public static final String REPOSITORY_COMPACTION_ENDPOINT = ADMIN_ENDPOINT + "/repository/compaction";

    private ApiConstants() {
    }

}
