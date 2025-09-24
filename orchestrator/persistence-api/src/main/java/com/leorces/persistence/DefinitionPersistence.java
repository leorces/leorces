package com.leorces.persistence;


import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;

import java.util.List;
import java.util.Optional;


/**
 * Persistence layer for managing process definitions.
 * Provides operations for storing, retrieving, and querying process definitions.
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

}
