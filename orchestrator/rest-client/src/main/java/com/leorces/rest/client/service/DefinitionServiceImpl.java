package com.leorces.rest.client.service;

import com.leorces.api.DefinitionService;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.rest.client.client.DefinitionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service("leorcesDefinitionService")
public class DefinitionServiceImpl implements DefinitionService {

    private final DefinitionClient definitionClient;

    @Override
    public List<ProcessDefinition> save(List<ProcessDefinition> definitions) {
        return definitionClient.save(definitions);
    }

    @Override
    public Optional<ProcessDefinition> findById(String definitionId) {
        return definitionClient.findById(definitionId);
    }

    @Override
    public PageableData<ProcessDefinition> findAll(Pageable pageable) {
        return definitionClient.findAll(pageable);
    }

}
