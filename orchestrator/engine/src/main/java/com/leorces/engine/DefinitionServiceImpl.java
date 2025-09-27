package com.leorces.engine;

import com.leorces.api.DefinitionService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.DefinitionPersistence;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class DefinitionServiceImpl implements DefinitionService {

    private final DefinitionPersistence persistence;

    @Override
    public List<ProcessDefinition> save(List<ProcessDefinition> definitions) {
        log.debug("Saving {} process definitions", definitions.size());
        return persistence.save(definitions);
    }

    @Override
    public Optional<ProcessDefinition> findById(String definitionId) {
        log.debug("Finding process definition by id: {}", definitionId);
        return persistence.findById(definitionId);
    }

    @Override
    public PageableData<ProcessDefinition> findAll(Pageable pageable) {
        log.debug("Finding all process definitions for pageable: {}", pageable);
        return persistence.findAll(pageable);
    }

}
