package com.leorces.engine.variables.handler;

import com.leorces.common.mapper.VariablesMapper;
import com.leorces.engine.variables.command.GetProcessVariablesCommand;
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
@DisplayName("GetProcessVariablesCommandHandler Tests")
class GetProcessVariablesCommandHandlerTest {

    private static final String PROCESS_ID = "process-id";

    @Mock
    private VariablePersistence variablePersistence;

    @Mock
    private VariablesMapper variablesMapper;

    @InjectMocks
    private GetProcessVariablesCommandHandler handler;

    @Test
    @DisplayName("Should return process variables as map")
    void shouldReturnProcessVariables() {
        // Given
        var variables = List.<Variable>of();
        var expectedMap = Map.<String, Object>of("pVar", "pVal");

        when(variablePersistence.findInProcess(PROCESS_ID)).thenReturn(variables);
        when(variablesMapper.toMap(variables)).thenReturn(expectedMap);

        var command = GetProcessVariablesCommand.of(PROCESS_ID);

        // When
        var result = handler.execute(command);

        // Then
        assertThat(result).isEqualTo(expectedMap);
    }

}
