package com.leorces.engine.definition.handler;

import com.leorces.engine.core.ResultCommandHandler;
import com.leorces.engine.definition.command.SaveDefinitionsCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.MessageActivityDefinition;
import com.leorces.persistence.DefinitionPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveDefinitionsCommandHandler implements ResultCommandHandler<SaveDefinitionsCommand, List<ProcessDefinition>> {

    private final DefinitionPersistence definitionPersistence;

    @Override
    public List<ProcessDefinition> execute(SaveDefinitionsCommand command) {
        var definitions = normalizeDefinitions(command.definitions());
        return definitionPersistence.save(definitions);
    }

    @Override
    public Class<SaveDefinitionsCommand> getCommandType() {
        return SaveDefinitionsCommand.class;
    }

    private List<ProcessDefinition> normalizeDefinitions(List<ProcessDefinition> definitions) {
        return definitions.stream()
                .map(this::normalize)
                .toList();
    }

    private ProcessDefinition normalize(ProcessDefinition definition) {
        return definition.toBuilder()
                .messages(normalizeMessages(definition))
                .build();
    }

    private List<String> normalizeMessages(ProcessDefinition definition) {
        var usedMessages = findUsedMessages(definition);
        return definition.messages().stream()
                .filter(usedMessages::contains)
                .toList();
    }

    private Set<String> findUsedMessages(ProcessDefinition definition) {
        return definition.activities().stream()
                .filter(MessageActivityDefinition.class::isInstance)
                .map(MessageActivityDefinition.class::cast)
                .map(MessageActivityDefinition::messageReference)
                .collect(Collectors.toSet());
    }

}
