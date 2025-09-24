package com.leorces.api;


import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;

import java.util.List;
import java.util.Optional;


/**
 * Service for managing process definitions.
 * Provides functionality to save, retrieve, and query process definitions.
 */
public interface DefinitionService {

    /**
     * Saves a list of process definitions.
     *
     * @param definitions the list of process definitions to save
     * @return the list of saved process definitions
     */
    List<ProcessDefinition> save(List<ProcessDefinition> definitions);

    /**
     * Finds a process definition by its unique identifier.
     *
     * @param definitionId the unique identifier of the process definition
     * @return an Optional containing the process definition if found, empty otherwise
     */
    Optional<ProcessDefinition> findById(String definitionId);

    /**
     * Retrieves all process definitions with pagination support.
     *
     * @param pageable the pagination parameters
     * @return pageable data containing process definitions
     */
    PageableData<ProcessDefinition> findAll(Pageable pageable);

}
