package com.leorces.persistence;

import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;

import java.util.List;

/**
 * Persistence layer for managing process and activity variables.
 * Provides operations for storing, updating, and retrieving variables within different scopes.
 */
public interface VariablePersistence {

    /**
     * Saves variables from a process execution.
     *
     * @param process the process containing variables to save
     * @return the list of saved variables
     */
    List<Variable> save(Process process);

    /**
     * Saves variables from an activity execution.
     *
     * @param activity the activity execution containing variables to save
     * @return the list of saved variables
     */
    List<Variable> save(ActivityExecution activity);

    /**
     * Updates a list of existing variables.
     *
     * @param variables the list of variables to update
     * @return the list of updated variables
     */
    List<Variable> update(List<Variable> variables);

    /**
     * Finds all variables for a process within specified scopes.
     *
     * @param processId the process identifier
     * @param scope     the list of scopes to search within
     * @return the list of variables matching the process ID and scopes
     */
    List<Variable> findAll(String processId, List<String> scope);

}
