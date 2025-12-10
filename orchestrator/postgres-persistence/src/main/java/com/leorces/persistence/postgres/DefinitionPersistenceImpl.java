package com.leorces.persistence.postgres;

import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.pagination.Pageable;
import com.leorces.model.pagination.PageableData;
import com.leorces.persistence.DefinitionPersistence;
import com.leorces.persistence.postgres.cache.DefinitionCache;
import com.leorces.persistence.postgres.mapper.DefinitionMapper;
import com.leorces.persistence.postgres.repository.DefinitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DefinitionPersistenceImpl implements DefinitionPersistence {

    private final DefinitionRepository definitionRepository;
    private final DefinitionMapper definitionMapper;
    private final DefinitionCache cache;
    private final DefinitionPersistenceImpl self;

    public DefinitionPersistenceImpl(DefinitionRepository definitionRepository,
                                     DefinitionMapper definitionMapper,
                                     DefinitionCache cache,
                                     @Lazy DefinitionPersistenceImpl self) {
        this.definitionRepository = definitionRepository;
        this.definitionMapper = definitionMapper;
        this.cache = cache;
        this.self = self;
    }

    @Override
    @Transactional
    public List<ProcessDefinition> save(List<ProcessDefinition> definitions) {
        return definitions.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public Optional<ProcessDefinition> findById(String definitionId) {
        return cache.findById(definitionId).or(() -> {
            var definition = definitionRepository.findById(definitionId)
                    .map(definitionMapper::toDefinition);

            definition.ifPresent(pd -> cache.putById(definitionId, pd));
            return definition;
        });
    }

    @Override
    public Optional<ProcessDefinition> findLatestByKey(String key) {
        return cache.findLatestByKey(key).or(() -> {
            var definition = definitionRepository.findLatestByKey(key)
                    .map(definitionMapper::toDefinition);

            definition.ifPresent(pd -> cache.putLatestByKey(key, pd));
            return definition;
        });
    }

    @Override
    public Optional<ProcessDefinition> findByKeyAndVersion(String key, Integer version) {
        return cache.findByKeyAndVersion(key, version).or(() -> {
            var definition = definitionRepository.findByKeyAndVersion(key, version)
                    .map(definitionMapper::toDefinition);

            definition.ifPresent(pd -> cache.putByKeyAndVersion(key, version, pd));
            return definition;
        });
    }

    @Override
    public PageableData<ProcessDefinition> findAll(Pageable pageable) {
        var pageableResult = definitionRepository.findAll(pageable);
        return new PageableData<>(definitionMapper.toDefinitions(pageableResult.data()), pageableResult.total());
    }

    @Transactional
    public ProcessDefinition save(ProcessDefinition definition) {
        try {
            return tryToSave(definition);
        } catch (DbActionExecutionException e) {
            return self.tryToSave(definition);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessDefinition tryToSave(ProcessDefinition definition) {
        var existingDefinitionOpt = findLatestByKey(definition.key());
        return existingDefinitionOpt.isEmpty()
                ? createNewProcessDefinition(definition, 1)
                : handleExistingDefinition(existingDefinitionOpt.get(), definition);
    }

    private ProcessDefinition handleExistingDefinition(ProcessDefinition definition, ProcessDefinition newDefinition) {
        return isSchemaChanged(definition, newDefinition)
                ? createNewProcessDefinition(newDefinition, definition.version() + 1)
                : definition;
    }

    private ProcessDefinition createNewProcessDefinition(ProcessDefinition definition, int version) {
        var entity = definitionMapper.toNewEntity(definition, version);
        var savedEntity = definitionRepository.save(entity);
        return definitionMapper.toDefinition(savedEntity);
    }

    private boolean isSchemaChanged(ProcessDefinition existingDefinition, ProcessDefinition newDefinition) {
        return !existingDefinition.metadata().schema().equals(newDefinition.metadata().schema());
    }

}
