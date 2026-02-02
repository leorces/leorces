package com.leorces.persistence.postgres;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.model.search.ProcessFilter;
import com.leorces.persistence.postgres.utils.ProcessDefinitionTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Process Persistence Integration Tests")
class ProcessPersistenceIT extends RepositoryIT {

    @Test
    @DisplayName("Should create and run a new process successfully and inherit suspended state from definition")
    void runInheritSuspended() {
        // Given
        var definition = definitionPersistence.save(List.of(ProcessDefinitionTestData.createOrderSubmittedProcessDefinition())).getFirst();
        definitionPersistence.suspendById(definition.id());
        var process = Process.builder()
                .definition(definitionPersistence.findById(definition.id()).get())
                .businessKey("test-suspended")
                .variables(List.of())
                .build();

        // When
        var result = processPersistence.run(process);

        // Then
        assertThat(result.suspended()).isTrue();
    }

    @Test
    @DisplayName("Should create and run a new process successfully")
    void run() {
        // Given
        var process = createOrderSubmittedProcess();

        // When
        var result = processPersistence.run(process);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.state()).isEqualTo(ProcessState.ACTIVE);
        assertThat(result.startedAt()).isNotNull();
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
        assertThat(result.definition()).isEqualTo(process.definition());
        assertThat(result.variables()).hasSize(process.variables().size());
        assertThat(result.variables()).allSatisfy(variable -> {
            assertThat(variable.id()).isNotNull();
            assertThat(variable.processId()).isEqualTo(result.id());
            assertThat(variable.createdAt()).isNotNull();
            assertThat(variable.updatedAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("Should complete an active process successfully")
    void complete() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());

        // When
        processPersistence.complete(process.id());
        var result = processPersistence.findById(process.id()).get();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(process.id());
        assertThat(result.state()).isEqualTo(ProcessState.COMPLETED);
        assertThat(result.completedAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should terminate an active process successfully")
    void terminate() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());

        // When
        processPersistence.terminate(process.id());
        var result = processPersistence.findById(process.id()).get();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(process.id());
        assertThat(result.state()).isEqualTo(ProcessState.TERMINATED);
        assertThat(result.completedAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should mark process as incident when error occurs")
    void incident() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());

        // When
        processPersistence.incident(process.id());
        var result = processPersistence.findById(process.id()).get();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(process.id());
        assertThat(result.state()).isEqualTo(ProcessState.INCIDENT);
        assertThat(result.updatedAt()).isNotNull();
        assertThat(result.completedAt()).isNull();
    }

    @Test
    @DisplayName("Should mark process as suspended")
    void suspendById() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());

        // When
        processPersistence.suspendById(process.id());
        var result = processPersistence.findById(process.id()).get();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(process.id());
        assertThat(result.suspended()).isEqualTo(true);
    }

    @Test
    @DisplayName("Should mark processes as suspended by definition id")
    void suspendByDefinitionId() {
        // Given
        var process1 = processPersistence.run(createOrderSubmittedProcess());
        var process2 = processPersistence.run(createOrderSubmittedProcess());

        // When
        processPersistence.suspendByDefinitionId(process1.definitionId());
        var result1 = processPersistence.findById(process1.id()).get();
        var result2 = processPersistence.findById(process2.id()).get();

        // Then
        assertThat(result1.suspended()).isEqualTo(true);
        assertThat(result2.suspended()).isEqualTo(true);
    }

    @Test
    @DisplayName("Should mark processes as suspended by definition key")
    void suspendByDefinitionKey() {
        // Given
        var process1 = processPersistence.run(createOrderSubmittedProcess());
        var process2 = processPersistence.run(createOrderSubmittedProcess());

        // When
        processPersistence.suspendByDefinitionKey(process1.definitionKey());
        var result1 = processPersistence.findById(process1.id()).get();
        var result2 = processPersistence.findById(process2.id()).get();

        // Then
        assertThat(result1.suspended()).isEqualTo(true);
        assertThat(result2.suspended()).isEqualTo(true);
    }

    @Test
    @DisplayName("Should resume process")
    void resumeById() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());
        processPersistence.suspendById(process.id());
        var suspendedProcess = processPersistence.findById(process.id()).get();
        assertThat(suspendedProcess.suspended()).isEqualTo(true);

        // When
        processPersistence.resumeById(process.id());
        var result = processPersistence.findById(process.id()).get();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(process.id());
        assertThat(result.suspended()).isEqualTo(false);
    }

    @Test
    @DisplayName("Should resume processes by definition id")
    void resumeByDefinitionId() {
        // Given
        var process1 = processPersistence.run(createOrderSubmittedProcess());
        var process2 = processPersistence.run(createOrderSubmittedProcess());
        processPersistence.suspendByDefinitionId(process1.definitionId());
        var suspendedProcess1 = processPersistence.findById(process1.id()).get();
        var suspendedProcess2 = processPersistence.findById(process2.id()).get();
        assertThat(suspendedProcess1.suspended()).isEqualTo(true);
        assertThat(suspendedProcess2.suspended()).isEqualTo(true);

        // When
        processPersistence.resumeByDefinitionId(process1.definitionId());
        var result1 = processPersistence.findById(process1.id()).get();
        var result2 = processPersistence.findById(process2.id()).get();

        // Then
        assertThat(result1.suspended()).isEqualTo(false);
        assertThat(result2.suspended()).isEqualTo(false);
    }

    @Test
    @DisplayName("Should resume processes by definition key")
    void resumeByDefinitionKey() {
        // Given
        var process1 = processPersistence.run(createOrderSubmittedProcess());
        var process2 = processPersistence.run(createOrderSubmittedProcess());
        processPersistence.suspendByDefinitionKey(process1.definitionKey());
        var suspendedProcess1 = processPersistence.findById(process1.id()).get();
        var suspendedProcess2 = processPersistence.findById(process2.id()).get();
        assertThat(suspendedProcess1.suspended()).isEqualTo(true);
        assertThat(suspendedProcess2.suspended()).isEqualTo(true);

        // When
        processPersistence.resumeByDefinitionId(process1.definitionId());
        var result1 = processPersistence.findById(process1.id()).get();
        var result2 = processPersistence.findById(process2.id()).get();

        // Then
        assertThat(result1.suspended()).isEqualTo(false);
        assertThat(result2.suspended()).isEqualTo(false);
    }

    @Test
    @DisplayName("Should delete an active process successfully")
    void delete() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());

        // When
        processPersistence.delete(process.id());
        var result = processPersistence.findById(process.id()).get();

        // Then
        assertThat(result.state()).isEqualTo(ProcessState.DELETED);
    }

    @Test
    @DisplayName("Should change process state")
    void changeState() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());

        // When
        processPersistence.changeState(process.id(), ProcessState.COMPLETED);
        var result = processPersistence.findById(process.id()).get();

        // Then
        assertThat(result.state()).isEqualTo(ProcessState.COMPLETED);
    }

    @Test
    @DisplayName("Should find executions for update")
    void findExecutionsForUpdate() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());

        // When
        var result = processPersistence.findExecutionsForUpdate(process.definitionId(), 10);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.stream().map(ProcessExecution::id)).contains(process.id());
    }

    @Test
    @DisplayName("Should find all fully completed processes for update")
    void findAllFullyCompletedForUpdate() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());
        processPersistence.complete(process.id());

        // When
        var result = processPersistence.findAllFullyCompletedForUpdate(10);

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Should update definition id for processes")
    void updateDefinitionId() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());
        var newDef = definitionPersistence.save(List.of(ProcessDefinitionTestData.createOrderSubmittedProcessDefinition())).getFirst();
        var newDefId = newDef.id();

        // When
        var count = processPersistence.updateDefinitionId(process.definitionId(), newDefId, 10);

        // Then
        assertThat(count).isPositive();
        var updatedProcess = processPersistence.findById(process.id()).get();
        assertThat(updatedProcess.definitionId()).isEqualTo(newDefId);
    }

    @Test
    @DisplayName("Should update definition id for specific process IDs")
    void updateDefinitionIdList() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());
        var newDef = definitionPersistence.save(List.of(ProcessDefinitionTestData.createOrderSubmittedProcessDefinition())).getFirst();
        var newDefId = newDef.id();

        // When
        var count = processPersistence.updateDefinitionId(newDefId, List.of(process.id()));

        // Then
        assertThat(count).isEqualTo(1);
        var updatedProcess = processPersistence.findById(process.id()).get();
        assertThat(updatedProcess.definitionId()).isEqualTo(newDefId);
    }

    @Test
    @DisplayName("Should find process by ID and return empty for non-existent ID")
    void findById() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());

        // When
        var result = processPersistence.findById(process.id());

        // Then
        assertThat(result).isPresent();
        var foundProcess = result.get();
        assertThat(foundProcess.id()).isEqualTo(process.id());
        assertThat(foundProcess.businessKey()).isEqualTo(process.businessKey());
        assertThat(foundProcess.state()).isEqualTo(process.state());
        assertThat(foundProcess.definition().id()).isEqualTo(process.definition().id());
        assertThat(foundProcess.variables()).hasSize(process.variables().size());

        // When & Then - non-existent process
        var nonExistentResult = processPersistence.findById("non-existent-id");
        assertThat(nonExistentResult).isEmpty();
    }

    @Test
    @DisplayName("Should find process execution by ID with activities and return empty for non-existent ID")
    void findExecutionById() {
        // Given
        var definition = definitionPersistence.save(List.of(ProcessDefinitionTestData.createOrderSubmittedProcessDefinition())).getFirst();
        definitionPersistence.suspendById(definition.id());
        var process = Process.builder()
                .definition(definitionPersistence.findById(definition.id()).get())
                .businessKey("test-execution-suspended")
                .variables(List.of())
                .build();
        var runResult = processPersistence.run(process);

        // When
        var result = processPersistence.findExecutionById(runResult.id());

        // Then
        assertThat(result).isPresent();
        var execution = result.get();
        assertThat(execution.id()).isEqualTo(runResult.id());
        assertThat(execution.definition().suspended()).isTrue();
        assertThat(execution.activities()).isNotNull();

        // When & Then - non-existent process
        var nonExistentResult = processPersistence.findExecutionById("non-existent-id");
        assertThat(nonExistentResult).isEmpty();
    }

    @Test
    @DisplayName("Should find processes by business key and return empty for non-existent key")
    void findByBusinessKey() {
        // Given
        var process1 = processPersistence.run(createOrderSubmittedProcess());
        var process2 = processPersistence.run(createOrderSubmittedProcess());
        var processFilter = ProcessFilter.builder().businessKey(process1.businessKey()).build();

        // When
        var result = processPersistence.findAll(processFilter);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().businessKey()).isEqualTo(process1.businessKey());

        // When & Then - non-existent business key
        var processFilter2 = ProcessFilter.builder().businessKey("non-existent-key").build();
        var nonExistentResult = processPersistence.findAll(processFilter2);
        assertThat(nonExistentResult).isEmpty();
    }

    @Test
    @DisplayName("Should find processes by variables and handle non-matching or empty variables")
    void findByVariables() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());
        var variables = Map.<String, Object>of(
                "order", "{\"number\":1234}",
                "client", "{\"firstName\":\"Json\",\"lastName\":\"Statement\"}"
        );
        var processFilter = ProcessFilter.builder().variables(variables).build();

        // When
        var result = processPersistence.findAll(processFilter);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(process.id());

        // When & Then - non-matching variables
        var nonMatchingVariables = Map.<String, Object>of("nonExistent", "value");
        var processFilter2 = ProcessFilter.builder().variables(nonMatchingVariables).build();
        var nonMatchingResult = processPersistence.findAll(processFilter2);
        assertThat(nonMatchingResult).isEmpty();

        // When & Then - empty variables map
        var processFilter3 = ProcessFilter.builder().variables(Map.of()).build();
        var emptyResult = processPersistence.findAll(processFilter3);
        assertThat(emptyResult).isEmpty();
    }

    @Test
    @DisplayName("Should find processes by business key and variables combination")
    void findByBusinessKeyAndVariables() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());
        var variables = Map.<String, Object>of(
                "order", "{\"number\":1234}",
                "client", "{\"firstName\":\"Json\",\"lastName\":\"Statement\"}"
        );
        var processFilter = ProcessFilter.builder()
                .businessKey(process.businessKey())
                .variables(variables)
                .build();

        // When
        var result = processPersistence.findAll(processFilter);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(process.id());
        assertThat(result.getFirst().businessKey()).isEqualTo(process.businessKey());

        // When & Then - non-matching business key
        var processFilter2 = ProcessFilter.builder()
                .businessKey("non-existent-key")
                .variables(variables)
                .build();
        var nonMatchingResult = processPersistence.findAll(processFilter2);
        assertThat(nonMatchingResult).isEmpty();

        // When & Then - empty variables map
        var processFilter3 = ProcessFilter.builder()
                .businessKey(process.businessKey())
                .variables(Map.of())
                .build();
        var emptyVariablesResult = processPersistence.findAll(processFilter3);
        assertThat(emptyVariablesResult).isNotEmpty();
        assertThat(emptyVariablesResult).hasSize(1);
    }

    @Test
    @DisplayName("Should find processes by business key and variables combination")
    void findByBusinessKeyAndDefinitionKey() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());
        var processFilter = ProcessFilter.builder()
                .businessKey(process.businessKey())
                .processDefinitionKey(process.definitionKey())
                .build();

        // When
        var result = processPersistence.findAll(processFilter);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(process.id());
        assertThat(result.getFirst().businessKey()).isEqualTo(process.businessKey());
    }

    @Test
    @DisplayName("Should find processes by business key and variables combination")
    void findByProcessId() {
        // Given
        var process = processPersistence.run(createOrderSubmittedProcess());
        var processFilter = ProcessFilter.builder()
                .processId(process.id())
                .build();

        // When
        var result = processPersistence.findAll(processFilter);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(process.id());
        assertThat(result.getFirst().businessKey()).isEqualTo(process.businessKey());
    }

    @Test
    @DisplayName("Should find all processes with pagination and verify pagination functionality")
    void findAll() {
        // Given
        var process1 = processPersistence.run(createOrderSubmittedProcess());
        var process2 = processPersistence.run(createOrderSubmittedProcess());
        var pageable = new Pageable(0, 10);

        // When
        var result = processPersistence.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.data()).isNotEmpty();
        assertThat(result.data()).hasSizeLessThanOrEqualTo(10);
        assertThat(result.total()).isPositive();
        var processIds = result.data().stream().map(Process::id).toList();
        assertThat(processIds).contains(process1.id(), process2.id());

        // When & Then - test pagination limit
        var limitedPageable = new Pageable(0, 1);
        var limitedResult = processPersistence.findAll(limitedPageable);
        assertThat(limitedResult.data()).hasSize(1);
        assertThat(limitedResult.total()).isEqualTo(result.total());
    }

}