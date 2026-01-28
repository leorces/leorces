package com.leorces.engine.admin.migration.handler;

import com.leorces.engine.admin.migration.command.GenerateProcessMigrationPlanCommand;
import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.job.migration.ProcessMigrationPlan;
import com.leorces.persistence.DefinitionPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateProcessMigrationPlanCommandHandler Tests")
class GenerateProcessMigrationPlanCommandHandlerTest {

    @Mock
    private DefinitionPersistence definitionPersistence;

    @InjectMocks
    private GenerateProcessMigrationPlanCommandHandler handler;

    @Test
    @DisplayName("Should generate migration plan with instructions for deleted activities")
    void shouldGenerateMigrationPlan() {
        // Given
        var definitionKey = "test-process";
        var fromVersion = 1;
        var toVersion = 2;
        var migration = ProcessMigrationPlan.builder()
                .definitionKey(definitionKey)
                .fromVersion(fromVersion)
                .toVersion(toVersion)
                .build();
        var command = new GenerateProcessMigrationPlanCommand(migration);

        var fromActivity = mock(ActivityDefinition.class);
        when(fromActivity.id()).thenReturn("activity1");
        var fromDefinition = mock(ProcessDefinition.class);
        when(fromDefinition.activities()).thenReturn(List.of(fromActivity));

        var toDefinition = mock(ProcessDefinition.class);
        when(toDefinition.activities()).thenReturn(List.of());

        when(definitionPersistence.findByKeyAndVersion(definitionKey, fromVersion)).thenReturn(Optional.of(fromDefinition));
        when(definitionPersistence.findByKeyAndVersion(definitionKey, toVersion)).thenReturn(Optional.of(toDefinition));

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result.instructions()).hasSize(1);
        assertThat(result.instructions().getFirst().fromActivityId()).isEqualTo("activity1");
        assertThat(result.instructions().getFirst().toActivityId()).isNull();
    }

}
