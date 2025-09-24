package com.leorces.api;


import com.leorces.model.runtime.process.Process;

import java.util.Map;


/**
 * Service for runtime process operations.
 * Provides functionality to start processes, manage variables, and correlate messages.
 */
public interface RuntimeService {

    /**
     * Starts a new process instance using the process definition ID.
     *
     * @param definitionId the unique identifier of the process definition
     * @return the started process instance
     */
    Process startProcessById(String definitionId);

    /**
     * Starts a new process instance using the process definition ID with initial variables.
     *
     * @param definitionId the unique identifier of the process definition
     * @param variables    the initial variables for the process instance
     * @return the started process instance
     */
    Process startProcessById(String definitionId, Map<String, Object> variables);

    /**
     * Starts a new process instance using the process definition ID with a business key.
     *
     * @param definitionId the unique identifier of the process definition
     * @param businessKey  the business key for the process instance
     * @return the started process instance
     */
    Process startProcessById(String definitionId, String businessKey);

    /**
     * Starts a new process instance using the process definition ID with business key and variables.
     *
     * @param definitionId the unique identifier of the process definition
     * @param businessKey  the business key for the process instance
     * @param variables    the initial variables for the process instance
     * @return the started process instance
     */
    Process startProcessById(String definitionId, String businessKey, Map<String, Object> variables);

    /**
     * Starts a new process instance using the process definition key.
     *
     * @param key the key of the process definition
     * @return the started process instance
     */
    Process startProcessByKey(String key);

    /**
     * Starts a new process instance using the process definition key with initial variables.
     *
     * @param key       the key of the process definition
     * @param variables the initial variables for the process instance
     * @return the started process instance
     */
    Process startProcessByKey(String key, Map<String, Object> variables);

    /**
     * Starts a new process instance using the process definition key with a business key.
     *
     * @param key         the key of the process definition
     * @param businessKey the business key for the process instance
     * @return the started process instance
     */
    Process startProcessByKey(String key, String businessKey);

    /**
     * Starts a new process instance using the process definition key with business key and variables.
     *
     * @param key         the key of the process definition
     * @param businessKey the business key for the process instance
     * @param variables   the initial variables for the process instance
     * @return the started process instance
     */
    Process startProcessByKey(String key, String businessKey, Map<String, Object> variables);

    /**
     * Sets a single variable in the specified execution context.
     *
     * @param executionId the unique identifier of the execution
     * @param key         the variable name
     * @param value       the variable value
     */
    void setVariable(String executionId, String key, Object value);

    /**
     * Sets multiple variables in the specified execution context.
     *
     * @param executionId the unique identifier of the execution
     * @param variables   the variables to set
     */
    void setVariables(String executionId, Map<String, Object> variables);

    /**
     * Sets a single local variable in the specified execution context.
     * Local variables are only visible within the current execution scope.
     *
     * @param executionId the unique identifier of the execution
     * @param key         the variable name
     * @param value       the variable value
     */
    void setVariableLocal(String executionId, String key, Object value);

    /**
     * Sets multiple local variables in the specified execution context.
     * Local variables are only visible within the current execution scope.
     *
     * @param executionId the unique identifier of the execution
     * @param variables   the local variables to set
     */
    void setVariablesLocal(String executionId, Map<String, Object> variables);

    /**
     * Correlates a message using message name and business key.
     *
     * @param messageName the name of the message to correlate
     * @param businessKey the business key for message correlation
     */
    void correlateMessage(String messageName, String businessKey);

    /**
     * Correlates a message using message name and correlation keys.
     *
     * @param messageName     the name of the message to correlate
     * @param correlationKeys the correlation keys for message matching
     */
    void correlateMessage(String messageName, Map<String, Object> correlationKeys);

    /**
     * Correlates a message using message name, business key, and process variables.
     *
     * @param messageName      the name of the message to correlate
     * @param businessKey      the business key for message correlation
     * @param processVariables the variables to set in the target process
     */
    void correlateMessage(String messageName, String businessKey, Map<String, Object> processVariables);

    /**
     * Correlates a message using message name, correlation keys, and process variables.
     *
     * @param messageName      the name of the message to correlate
     * @param correlationKeys  the correlation keys for message matching
     * @param processVariables the variables to set in the target process
     */
    void correlateMessage(String messageName, Map<String, Object> correlationKeys, Map<String, Object> processVariables);

    /**
     * Correlates a message using all available correlation parameters.
     *
     * @param messageName      the name of the message to correlate
     * @param businessKey      the business key for message correlation
     * @param processVariables the variables to set in the target process
     * @param correlationKeys  the correlation keys for message matching
     */
    void correlateMessage(String messageName, String businessKey, Map<String, Object> processVariables, Map<String, Object> correlationKeys);

}
