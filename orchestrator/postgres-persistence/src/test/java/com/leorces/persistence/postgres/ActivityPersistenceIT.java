package com.leorces.persistence.postgres;

import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.persistence.postgres.utils.ActivityTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Activity Persistence Integration Tests")
class ActivityPersistenceIT extends RepositoryIT {

    @Test
    @DisplayName("Should create a new activity successfully")
    void schedule() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = ActivityTestData.createNotificationToClientActivityExecution(process);

        // When
        var result = activityPersistence.schedule(activity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.state()).isEqualTo(ActivityState.SCHEDULED);
        assertThat(result.definitionId()).isEqualTo(activity.definitionId());
        assertThat(result.process()).isEqualTo(activity.process());
        assertThat(result.retries()).isEqualTo(activity.retries());
        assertThat(result.variables()).hasSize(activity.variables().size());
        assertThat(result.variables()).allSatisfy(variable -> {
            assertThat(variable.id()).isNotNull();
            assertThat(variable.createdAt()).isNotNull();
            assertThat(variable.updatedAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("Should run a created activity successfully")
    void run() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = ActivityTestData.createNotificationToClientActivityExecution(process);

        // When
        var result = activityPersistence.run(activity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.state()).isEqualTo(ActivityState.ACTIVE);
        assertThat(result.definitionId()).isEqualTo(activity.definitionId());
        assertThat(result.process()).isEqualTo(activity.process());
        assertThat(result.retries()).isEqualTo(activity.retries());
        assertThat(result.variables()).hasSize(activity.variables().size());
    }

    @Test
    @DisplayName("Should complete an active activity successfully")
    void complete() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));

        // When
        var result = activityPersistence.complete(activity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(activity.id());
        assertThat(result.state()).isEqualTo(ActivityState.COMPLETED);
        assertThat(result.definitionId()).isEqualTo(activity.definitionId());
        assertThat(result.process()).isEqualTo(activity.process());
        assertThat(result.retries()).isEqualTo(activity.retries());
    }

    @Test
    @DisplayName("Should terminate an active activity successfully")
    void terminate() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));

        // When
        var result = activityPersistence.terminate(activity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(activity.id());
        assertThat(result.state()).isEqualTo(ActivityState.TERMINATED);
        assertThat(result.definitionId()).isEqualTo(activity.definitionId());
        assertThat(result.process()).isEqualTo(activity.process());
        assertThat(result.retries()).isEqualTo(activity.retries());
    }

    @Test
    @DisplayName("Should mark activity as failed successfully")
    void fail() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));

        // When
        var result = activityPersistence.fail(activity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(activity.id());
        assertThat(result.state()).isEqualTo(ActivityState.FAILED);
        assertThat(result.definitionId()).isEqualTo(activity.definitionId());
        assertThat(result.process()).isEqualTo(activity.process());
        assertThat(result.retries()).isEqualTo(activity.retries());
    }

    @Test
    void deleteAllActive() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity1 = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        var activity2 = activityPersistence.run(ActivityTestData.createNotificationToSellerActivityExecution(process));

        var active = activityPersistence.findActive(process.id());
        assertThat(active).hasSize(2);

        // When
        activityPersistence.deleteAllActive(process.id(), List.of(activity1.definitionId(), activity2.definitionId()));

        // Then
        var activeAfterDeletion = activityPersistence.findActive(process.id());
        assertThat(activeAfterDeletion).hasSize(0);
    }

    @Test
    @DisplayName("Should find activity by ID and return empty for non-existent ID")
    void findById() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));

        // When
        var result = activityPersistence.findById(activity.id());

        // Then
        assertThat(result).isPresent();
        var foundActivity = result.get();
        assertThat(foundActivity.id()).isEqualTo(activity.id());
        assertThat(foundActivity.definitionId()).isEqualTo(activity.definitionId());
        assertThat(foundActivity.state()).isEqualTo(activity.state());
        assertThat(foundActivity.variables()).hasSize(4);

        // When & Then - non-existent activity
        var nonExistentResult = activityPersistence.findById("non-existent-id");
        assertThat(nonExistentResult).isEmpty();
    }

    @Test
    @DisplayName("Should find activity by definition ID and return empty for non-existent definition")
    void findByDefinitionId() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));

        // When
        var result = activityPersistence.findByDefinitionId(activity.processId(), activity.definitionId());

        // Then
        assertThat(result).isPresent();
        var foundActivity = result.get();
        assertThat(foundActivity.id()).isEqualTo(activity.id());
        assertThat(foundActivity.definitionId()).isEqualTo(activity.definitionId());
        assertThat(foundActivity.processId()).isEqualTo(activity.processId());

        // When & Then - non-existent definition
        var nonExistentResult = activityPersistence.findByDefinitionId(activity.processId(), "non-existent-definition");
        assertThat(nonExistentResult).isEmpty();
    }

    @Test
    @DisplayName("Should find active activities by process ID and definition IDs")
    void findActive() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity1 = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        var activity2 = activityPersistence.run(ActivityTestData.createNotificationToSellerActivityExecution(process));

        var processId = activity1.processId();
        var definitionIds = List.of(activity1.definitionId(), activity2.definitionId());

        // When
        var result = activityPersistence.findActive(processId, definitionIds);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("id").containsExactlyInAnyOrder(
                activity1.id(), activity2.id()
        );
        assertThat(result).allSatisfy(activity -> {
            assertThat(activity.state()).isEqualTo(ActivityState.ACTIVE);
            assertThat(activity.processId()).isEqualTo(processId);
        });
    }

    @Test
    @DisplayName("Should find all active activities by process ID")
    void testFindActive() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity1 = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        var activity2 = activityPersistence.run(ActivityTestData.createNotificationToSellerActivityExecution(process));
        activityPersistence.complete(activity2); // Complete one activity

        var processId = activity1.processId();

        // When
        var result = activityPersistence.findActive(processId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(activity1.id());
        assertThat(result.getFirst().state()).isEqualTo(ActivityState.ACTIVE);
        assertThat(result.getFirst().processId()).isEqualTo(processId);
    }

    @Test
    @DisplayName("Should find all timed out activities")
    void findTimedOut() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity1 = ActivityTestData.createNotificationToClientActivityExecution(process).toBuilder()
                .timeout(LocalDateTime.now().minusMinutes(1))
                .build();
        var activity2 = ActivityTestData.createNotificationToSellerActivityExecution(process).toBuilder()
                .timeout(LocalDateTime.now().minusMinutes(1))
                .build();

        activityPersistence.run(activity1);
        activityPersistence.run(activity2);

        // When
        var result = activityPersistence.findTimedOut();

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should poll activities by topic and process definition key")
    void poll() {
        // Given - This method interacts with ActivityQueue which requires specific setup
        var processDefinitionKey = "order-submitted-process";
        var topic = "notification";
        var limit = 5;

        // When
        var result = activityPersistence.poll(topic, processDefinitionKey, limit);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty(); // No activities in queue initially
    }

    @Test
    @DisplayName("Should check if all activities are completed by process ID and parent definition ID")
    void isAllCompleted() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity1 = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        var activity2 = activityPersistence.run(ActivityTestData.createNotificationToSellerActivityExecution(process));
        activityPersistence.complete(activity1);
        activityPersistence.complete(activity2);

        var processId = activity1.processId();
        var parentDefinitionId = "Gateway_0t354xy"; // From test data - parent gateway

        // When
        var result = activityPersistence.isAllCompleted(processId, parentDefinitionId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should check if all process activities are completed")
    void testIsAllCompleted() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity1 = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        activityPersistence.complete(activity1);

        var processId = activity1.processId();

        // When
        var result = activityPersistence.isAllCompleted(processId);

        // Then
        assertThat(result).isTrue();

        // When & Then - with active activity
        var activity2 = activityPersistence.run(ActivityTestData.createNotificationToSellerActivityExecution(process)
                .toBuilder()
                .process(activity1.process())
                .build());
        activityPersistence.run(activity2);

        var resultWithActive = activityPersistence.isAllCompleted(processId);
        assertThat(resultWithActive).isFalse();
    }

    @Test
    @DisplayName("Should check if specific activities are all completed by definition IDs")
    void testIsAllCompleted1() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity1 = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        var activity2 = activityPersistence.run(ActivityTestData.createNotificationToSellerActivityExecution(process));
        activityPersistence.complete(activity1);

        var processId = activity1.processId();
        var definitionIds = List.of(activity1.definitionId(), activity2.definitionId());

        // When
        var result = activityPersistence.isAllCompleted(processId, definitionIds);

        // Then
        assertThat(result).isFalse(); // One activity is still active

        // When & Then - complete the second activity
        activityPersistence.complete(activity2);
        var resultAfterCompletion = activityPersistence.isAllCompleted(processId, definitionIds);
        assertThat(resultAfterCompletion).isTrue();
    }

}