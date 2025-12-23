package com.leorces.persistence.postgres.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.leorces.model.definition.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class DefinitionCache {

    private static final String FIND_BY_ID_CACHE_NAME = "findById-%s";
    private static final String FIND_LATEST_BY_KEY_CACHE_NAME = "findLatestByKey-%s";
    private static final String FIND_BY_KEY_AND_VERSION_CACHE_NAME = "findByKeyAndVersion-%s-%d";

    private final Cache<String, ProcessDefinition> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    public Optional<ProcessDefinition> findById(String definitionId) {
        return Optional.ofNullable(cache.getIfPresent(FIND_BY_ID_CACHE_NAME.formatted(definitionId)));
    }

    public void putById(String definitionId, ProcessDefinition definition) {
        cache.put(FIND_BY_ID_CACHE_NAME.formatted(definitionId), definition);
    }

    public Optional<ProcessDefinition> findLatestByKey(String key) {
        return Optional.ofNullable(cache.getIfPresent(FIND_LATEST_BY_KEY_CACHE_NAME.formatted(key)));
    }

    public void putLatestByKey(String key, ProcessDefinition definition) {
        cache.put(FIND_LATEST_BY_KEY_CACHE_NAME.formatted(key), definition);
    }

    public Optional<ProcessDefinition> findByKeyAndVersion(String key, Integer version) {
        return Optional.ofNullable(cache.getIfPresent(FIND_BY_KEY_AND_VERSION_CACHE_NAME.formatted(key, version)));
    }

    public void putByKeyAndVersion(String key, Integer version, ProcessDefinition definition) {
        cache.put(FIND_BY_KEY_AND_VERSION_CACHE_NAME.formatted(key, version), definition);
    }

    public void putLatest(ProcessDefinition definition) {
        putById(definition.id(), definition);
        putLatestByKey(definition.key(), definition);
        putByKeyAndVersion(definition.key(), definition.version(), definition);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

}
