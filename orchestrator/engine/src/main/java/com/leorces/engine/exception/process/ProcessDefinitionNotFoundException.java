package com.leorces.engine.exception.process;

public class ProcessDefinitionNotFoundException extends RuntimeException {

    public ProcessDefinitionNotFoundException(String definitionIdentifier) {
        super("Process definition not found: %s".formatted(definitionIdentifier));
    }

    public static ProcessDefinitionNotFoundException byKey(String key) {
        return new ProcessDefinitionNotFoundException(
                "Process definition not found by key: %s".formatted(key)
        );
    }

    public static ProcessDefinitionNotFoundException byKeyAndVersion(String key, Integer version) {
        return new ProcessDefinitionNotFoundException(
                "Process definition not found by key: %s and version: %s".formatted(key, version)
        );
    }

    public static ProcessDefinitionNotFoundException byId(String id) {
        return new ProcessDefinitionNotFoundException(
                "Process definition not found by id: %s".formatted(id)
        );
    }

}
