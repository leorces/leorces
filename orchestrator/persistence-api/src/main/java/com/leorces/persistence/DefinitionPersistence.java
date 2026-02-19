package com.leorces.persistence;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;

import java.util.List;
import java.util.Optional;

/**
 * Persistence layer for managing process definitions.
 * Provides operations for storing, retrieving, querying, suspending, and resuming process definitions.
 */
public interface DefinitionPersistence {

    /**
     * Saves a list of process definitions.
     *
     * @param processDefinitions the list of process definitions to save
     * @return the list of saved process definitions with generated identifiers
     */
    List<ProcessDefinition> save(List<ProcessDefinition> processDefinitions);

    /**
     * Finds a process definition by its unique identifier.
     *
     * @param id the unique identifier of the process definition
     * @return an optional containing the process definition if found, empty otherwise
     */
    Optional<ProcessDefinition> findById(String id);

    /**
     * Finds a process definition by its unique identifier with all details.
     * Unlike findById, this method may return additional details or skip caching.
     *
     * @param id the unique identifier of the process definition
     * @return an optional containing the full process definition if found, empty otherwise
     */
    Optional<ProcessDefinition> findFullById(String id);

    /**
     * Finds the latest version of a process definition by its key.
     *
     * @param key the process definition key
     * @return an optional containing the latest process definition if found, empty otherwise
     */
    Optional<ProcessDefinition> findLatestByKey(String key);

    /**
     * Finds a process definition by its key and specific version number.
     *
     * @param key     the process definition key
     * @param version the specific version number
     * @return an optional containing the process definition if found, empty otherwise
     */
    Optional<ProcessDefinition> findByKeyAndVersion(String key, Integer version);

    /**
     * Retrieves all process definitions with pagination support.
     *
     * @param pageable the pagination parameters
     * @return pageable data containing process definitions and total count
     */
    PageableData<ProcessDefinition> findAll(Pageable pageable);

    /**
     * Suspends a process definition by its unique identifier.
     * After suspension, all new process instances of this definition should not start.
     *
     * @param definitionId the unique identifier of the process definition to suspend
     */
    void suspendById(String definitionId);

    /**
     * Suspends all versions of a process definition by its key.
     * After suspension, all new process instances for this key should not start.
     *
     * @param definitionKey the key of the process definition to suspend
     */
    void suspendByKey(String definitionKey);

    /**
     * Resumes a suspended process definition by its unique identifier.
     * After resuming, new process instances can be started for this definition.
     *
     * @param definitionId the unique identifier of the process definition to resume
     */
    void resumeById(String definitionId);

    /**
     * Resumes all suspended versions of a process definition by its key.
     * After resuming, new process instances can be started for this key.
     *
     * @param definitionKey the key of the process definition to resume
     */
    void resumeByKey(String definitionKey);

}
