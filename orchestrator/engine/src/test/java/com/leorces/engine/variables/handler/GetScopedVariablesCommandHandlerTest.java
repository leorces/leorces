package com.leorces.engine.variables.handler;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.variables.command.GetScopedVariablesCommand;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.VariablePersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetScopedVariablesCommandHandler Tests")
class GetScopedVariablesCommandHandlerTest {

    private static final String PROCESS_ID = "process-id";
    private static final List<String> SCOPE = List.of("activity-id", "process-def-id");

    @Mock
    private VariablePersistence variablePersistence;

    @Mock
    private VariablesMapper variablesMapper;

    @Mock
    private ActivityExecution activity;

    @InjectMocks
    private GetScopedVariablesCommandHandler handler;

    @Test
    @DisplayName("Should return scoped variables as map")
    void shouldReturnScopedVariables() {
        // Given
        var variables = List.<Variable>of();
        var expectedMap = Map.<String, Object>of("var1", "val1");

        when(activity.processId()).thenReturn(PROCESS_ID);
        when(activity.scope()).thenReturn(SCOPE);
        when(variablePersistence.findInScope(PROCESS_ID, SCOPE)).thenReturn(variables);
        when(variablesMapper.toMap(variables, SCOPE)).thenReturn(expectedMap);

        var command = GetScopedVariablesCommand.of(activity);

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isEqualTo(expectedMap);
    }

}
