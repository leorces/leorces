package com.leorces.persistence.postgres;

import com.leorces.model.pagination.Pageable;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.model.runtime.process.Process;
import com.leorces.model.runtime.process.ProcessExecution;
import com.leorces.model.runtime.process.ProcessState;
import com.leorces.model.runtime.variable.Variable;
import com.leorces.persistence.postgres.entity.HistoryEntity;
import com.leorces.persistence.postgres.utils.ProcessDefinitionTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("History Persistence Integration Tests")
class HistoryPersistenceIT extends RepositoryIT {

    private static final String TEST_PROCESS_ID = "test-process-id";
    private static final String TEST_ROOT_PROCESS_ID = "test-root-process-id";
    private static final String TEST_BUSINESS_KEY = "test-business-key";
    private static final String TEST_ACTIVITY_ID = "test-activity-id";

    @Test
    @DisplayName("Should successfully save single process execution to history")
    void saveSingleProcessExecution() {
        // Given
        var process = createTestProcess();
        var processExecution = createTestProcessExecution(process);

        // When
        historyPersistence.save(List.of(processExecution));

        // Then
        var historyEntities = StreamSupport.stream(historyRepository.findAll().spliterator(), false).toList();
        assertThat(historyEntities).hasSize(1);

        var historyEntity = historyEntities.getFirst();
        assertThat(historyEntity.getProcessId()).isEqualTo(processExecution.id());
        assertThat(historyEntity.getRootProcessId()).isEqualTo(processExecution.rootProcessId());
        assertThat(historyEntity.getParentProcessId()).isEqualTo(processExecution.parentId());
        assertThat(historyEntity.getBusinessKey()).isEqualTo(processExecution.businessKey());
        assertThat(historyEntity.getData()).isNotNull();
        assertThat(historyEntity.getCreatedAt()).isNotNull();
        assertThat(historyEntity.getUpdatedAt()).isNotNull();
        assertThat(historyEntity.getStartedAt()).isNotNull();
        assertThat(historyEntity.getCompletedAt()).isNotNull();

        // Verify original process and activities were deleted
        assertThat(processRepository.existsById(processExecution.id())).isFalse();
        processExecution.activities().forEach(activity ->
                assertThat(activityRepository.existsById(activity.id())).isFalse()
        );
        processExecution.variables().forEach(variable ->
                assertThat(variableRepository.existsById(variable.id())).isFalse()
        );
    }

    @Test
    @DisplayName("Should successfully save multiple process executions to history")
    void saveMultipleProcessExecutions() {
        // Given
        var process1 = createTestProcess();
        var process2 = createTestProcess();
        var processExecution1 = createTestProcessExecution(process1);
        var processExecution2 = createTestProcessExecution(process2).toBuilder()
                .id("process-2-id")
                .businessKey("business-key-2")
                .build();

        // When
        historyPersistence.save(List.of(processExecution1, processExecution2));

        // Then
        var historyEntities = StreamSupport.stream(historyRepository.findAll().spliterator(), false).toList();
        assertThat(historyEntities).hasSize(2);

        var processIds = historyEntities.stream()
                .map(HistoryEntity::getProcessId)
                .toList();

        assertThat(processIds).containsExactlyInAnyOrder(
                processExecution1.id(),
                processExecution2.id()
        );

        // Verify all original processes and activities were deleted
        assertThat(processRepository.existsById(processExecution1.id())).isFalse();
        assertThat(processRepository.existsById(processExecution2.id())).isFalse();
    }

    @Test
    @DisplayName("Should handle empty process execution list gracefully")
    void saveEmptyProcessExecutionList() {
        // Given
        var emptyList = Collections.<ProcessExecution>emptyList();

        // When
        historyPersistence.save(emptyList);

        // Then
        var historyEntities = StreamSupport.stream(historyRepository.findAll().spliterator(), false).toList();
        assertThat(historyEntities).isEmpty();
    }

    @Test
    @DisplayName("Should successfully find all process executions with default pagination")
    void findAllWithDefaultPagination() {
        // Given
        var process = createTestProcess();
        var processExecution = createTestProcessExecution(process);
        historyPersistence.save(List.of(processExecution));

        var pageable = new Pageable(0, 10);

        // When
        var result = historyPersistence.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        assertThat(result.total()).isEqualTo(1L);

        var foundExecution = result.data().getFirst();
        assertThat(foundExecution.id()).isEqualTo(processExecution.id());
        assertThat(foundExecution.businessKey()).isEqualTo(processExecution.businessKey());
        assertThat(foundExecution.state()).isEqualTo(processExecution.state());
    }

    @Test
    @DisplayName("Should find all process executions with custom pagination")
    void findAllWithCustomPagination() {
        // Given
        var process = createTestProcess();

        // Create 5 process executions
        for (int i = 1; i <= 5; i++) {
            var processExecution = createTestProcessExecution(process).toBuilder()
                    .id("process-" + i)
                    .businessKey("business-key-" + i)
                    .build();
            historyPersistence.save(List.of(processExecution));
        }

        var pageable = new Pageable(2, 2); // Skip 2, take 2

        // When
        var result = historyPersistence.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(2);
        assertThat(result.total()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should return empty result when no history exists")
    void findAllFromEmptyHistory() {
        // Given
        var pageable = new Pageable(0, 10);

        // When
        var result = historyPersistence.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.data()).isEmpty();
        assertThat(result.total()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should handle pagination with zero limit")
    void findAllWithZeroLimit() {
        // Given
        var process = createTestProcess();
        var processExecution = createTestProcessExecution(process);
        historyPersistence.save(List.of(processExecution));

        var pageable = new Pageable(0, 0);

        // When
        var result = historyPersistence.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.data()).isEmpty();
        assertThat(result.total()).isEqualTo(1L); // Total should still reflect actual count
    }

    @Test
    @DisplayName("Should handle pagination with offset beyond available records")
    void findAllWithOffsetBeyondRecords() {
        // Given
        var process = createTestProcess();
        var processExecution = createTestProcessExecution(process);
        historyPersistence.save(List.of(processExecution));

        var pageable = new Pageable(10, 5); // Offset beyond available records

        // When
        var result = historyPersistence.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.data()).isEmpty();
        assertThat(result.total()).isEqualTo(1L);
    }

    private Process createTestProcess() {
        var processDefinition = definitionPersistence.save(
                List.of(ProcessDefinitionTestData.createOrderSubmittedProcessDefinition())
        ).getFirst();

        return Process.builder()
                .id(TEST_PROCESS_ID)
                .rootProcessId(TEST_ROOT_PROCESS_ID)
                .businessKey(TEST_BUSINESS_KEY)
                .state(ProcessState.COMPLETED)
                .definition(processDefinition)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now().minusMinutes(30))
                .completedAt(LocalDateTime.now())
                .build();
    }

    private ProcessExecution createTestProcessExecution(Process process) {
        var variables = List.of(
                Variable.builder()
                        .id("var-1")
                        .varKey("testVariable")
                        .varValue("testValue")
                        .type("STRING")
                        .processId(process.id())
                        .executionId(null)
                        .executionDefinitionId(null)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        var activities = List.of(createTestActivity(TEST_ACTIVITY_ID));

        return ProcessExecution.builder()
                .id(process.id())
                .rootProcessId(process.rootProcessId())
                .parentId(process.parentId())
                .businessKey(process.businessKey())
                .variables(variables)
                .activities(activities)
                .state(ProcessState.COMPLETED)
                .definition(process.definition())
                .createdAt(process.createdAt())
                .updatedAt(process.updatedAt())
                .startedAt(process.startedAt())
                .completedAt(process.completedAt())
                .build();
    }

    private Activity createTestActivity(String activityId) {
        return Activity.builder()
                .id(activityId)
                .definitionId("test-definition-id")
                .variables(List.of())
                .state(ActivityState.COMPLETED)
                .retries(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }

}