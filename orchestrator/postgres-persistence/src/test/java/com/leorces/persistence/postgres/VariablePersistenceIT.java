package com.leorces.persistence.postgres;

import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.postgres.utils.VariableTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VariablePersistenceIT extends RepositoryIT {

    @Test
    @DisplayName("Should save variables from process and return them with generated IDs")
    void saveByProcess() {
        // Given
        var orderVariable = VariableTestData.createOrderVariable();
        var clientVariable = VariableTestData.createClientVariable();
        var process = runProcess().toBuilder()
                .variables(List.of(orderVariable, clientVariable))
                .build();

        // When
        var result = variablePersistence.save(process);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        var savedOrderVariable = result.stream()
                .filter(v -> "order".equals(v.varKey()))
                .findFirst()
                .orElseThrow();
        var savedClientVariable = result.stream()
                .filter(v -> "client".equals(v.varKey()))
                .findFirst()
                .orElseThrow();

        assertNotNull(savedOrderVariable.id());
        assertEquals(process.id(), savedOrderVariable.processId());
        assertEquals("order", savedOrderVariable.varKey());
        assertEquals("{\"number\":1234}", savedOrderVariable.varValue());
        assertEquals("map", savedOrderVariable.type());
        assertNotNull(savedOrderVariable.createdAt());

        assertNotNull(savedClientVariable.id());
        assertEquals(process.id(), savedClientVariable.processId());
        assertEquals("client", savedClientVariable.varKey());
        assertEquals("{\"firstName\":\"Json\",\"lastName\":\"Statement\"}", savedClientVariable.varValue());
        assertEquals("map", savedClientVariable.type());
        assertNotNull(savedClientVariable.createdAt());
    }

    @Test
    @DisplayName("Should save variables from activity and return them with generated IDs")
    void saveByActivity() {
        // Given
        var process = runProcess();
        var activity = activityPersistence.schedule(
                ActivityExecution.builder()
                        .process(process)
                        .definitionId("NotificationToClient")
                        .build()
        );
        var activityWithVariables = activity.toBuilder()
                .variables(List.of(VariableTestData.createOrderVariable()))
                .build();

        // When
        var result = variablePersistence.save(activityWithVariables);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        var savedVariable = result.getFirst();
        assertNotNull(savedVariable.id());
        assertEquals(process.id(), savedVariable.processId());
        assertEquals(activity.id(), savedVariable.executionId());
        assertEquals("order", savedVariable.varKey());
    }

    @Test
    @DisplayName("Should update existing variables and return updated versions")
    void updateVariables() {
        // Given
        var orderVariable = VariableTestData.createOrderVariable();
        var clientVariable = VariableTestData.createClientVariable();
        var process = runProcess().toBuilder()
                .variables(List.of(orderVariable, clientVariable))
                .build();

        var savedVariables = variablePersistence.save(process);
        var variableToUpdate = savedVariables.getFirst().toBuilder()
                .varValue("{\"number\":5678}")
                .build();
        var updatedVariables = List.of(variableToUpdate);

        // When
        var result = variablePersistence.update(updatedVariables);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        var updatedVariable = result.getFirst();
        assertEquals(variableToUpdate.id(), updatedVariable.id());
        assertEquals(variableToUpdate.processId(), updatedVariable.processId());
        assertEquals("order", updatedVariable.varKey());
        assertEquals("{\"number\":5678}", updatedVariable.varValue());
        assertEquals("map", updatedVariable.type());
        assertNotNull(updatedVariable.updatedAt());
    }

    @Test
    @DisplayName("Should find all variables by processId and scope")
    void findInScopeVariables() {
        // Given
        var orderVariable = VariableTestData.createOrderVariable();
        var clientVariable = VariableTestData.createClientVariable();
        var process = runProcess().toBuilder()
                .variables(List.of(orderVariable, clientVariable))
                .build();

        var savedVariables = variablePersistence.save(process);
        var processId = process.id();

        // Extract execution definition IDs from saved variables for scope filtering
        var executionDefinitionIds = savedVariables.stream()
                .map(Variable::executionDefinitionId)
                .distinct()
                .toList();

        // When
        var result = variablePersistence.findInScope(processId, executionDefinitionIds);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        var foundOrderVariable = result.stream()
                .filter(v -> "order".equals(v.varKey()))
                .findFirst()
                .orElseThrow();
        var foundClientVariable = result.stream()
                .filter(v -> "client".equals(v.varKey()))
                .findFirst()
                .orElseThrow();

        assertEquals(processId, foundOrderVariable.processId());
        assertEquals("order", foundOrderVariable.varKey());
        assertEquals("{\"number\":1234}", foundOrderVariable.varValue());
        assertEquals("map", foundOrderVariable.type());

        assertEquals(processId, foundClientVariable.processId());
        assertEquals("client", foundClientVariable.varKey());
        assertEquals("{\"firstName\":\"Json\",\"lastName\":\"Statement\"}", foundClientVariable.varValue());
        assertEquals("map", foundClientVariable.type());
    }

    @Test
    @DisplayName("Should return empty list when no variables found for processId and scope")
    void findInScopeVariablesWhenEmpty() {
        // Given
        var nonExistentProcessId = "non-existent-process-id";
        var scope = List.of("order", "client");

        // When
        var result = variablePersistence.findInScope(nonExistentProcessId, scope);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all variables in process")
    void findInProcessVariables() {
        // Given
        var orderVariable = VariableTestData.createOrderVariable();
        var clientVariable = VariableTestData.createClientVariable();
        var process = runProcess().toBuilder()
                .variables(List.of(orderVariable, clientVariable))
                .build();

        variablePersistence.save(process);
        var processId = process.id();

        // When
        var result = variablePersistence.findInProcess(processId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        var foundOrderVariable = result.stream()
                .filter(v -> "order".equals(v.varKey()))
                .findFirst()
                .orElseThrow();
        var foundClientVariable = result.stream()
                .filter(v -> "client".equals(v.varKey()))
                .findFirst()
                .orElseThrow();

        assertEquals(processId, foundOrderVariable.processId());
        assertEquals("order", foundOrderVariable.varKey());
        assertEquals("{\"number\":1234}", foundOrderVariable.varValue());
        assertEquals("map", foundOrderVariable.type());

        assertEquals(processId, foundClientVariable.processId());
        assertEquals("client", foundClientVariable.varKey());
        assertEquals("{\"firstName\":\"Json\",\"lastName\":\"Statement\"}", foundClientVariable.varValue());
        assertEquals("map", foundClientVariable.type());
    }

    @Test
    @DisplayName("Should return empty list when no variables found for process")
    void findInProcessVariablesWhenEmpty() {
        // Given
        var nonExistentProcessId = "non-existent-process-id";

        // When
        var result = variablePersistence.findInProcess(nonExistentProcessId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private Process runProcess() {
        var process = createOrderSubmittedProcess().toBuilder()
                .variables(List.of())
                .build();
        return processPersistence.run(process);
    }

}
