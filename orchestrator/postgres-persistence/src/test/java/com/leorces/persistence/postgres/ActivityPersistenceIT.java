package com.leorces.persistence.postgres;

import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.persistence.postgres.utils.ActivityTestData;
import com.leorces.persistence.postgres.utils.VariableTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Activity Persistence Integration Tests")
class ActivityPersistenceIT extends RepositoryIT {

    @Test
    @DisplayName("Should delete all active activities successfully")
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
        assertThat(foundActivity.variables()).hasSize(2);

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
        var result = activityPersistence.findTimedOut(10);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should poll activities by topic and process definition key")
    void poll() {
        // Given
        var processDefinitionKey = "order-submitted-process";
        var topic = "notification";
        var limit = 5;

        // When
        var result = activityPersistence.poll(topic, processDefinitionKey, limit);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
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
    void isAllCompletedByProcessId() {
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
    void isAllCompletedByDefinitionIds() {
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

    @Test
    @DisplayName("Should change activity state by ID and update timestamp")
    void changeState() {
        // Given
        var process = runOrderSubmittedProcess();
        var scheduled = activityPersistence.schedule(ActivityTestData.createNotificationToClientActivityExecution(process));

        // When
        activityPersistence.changeState(scheduled.id(), ActivityState.FAILED);

        // Then
        var updatedActivity = activityPersistence.findById(scheduled.id());
        assertThat(updatedActivity).isPresent();
        assertThat(updatedActivity.get().state()).isEqualTo(ActivityState.FAILED);
    }

    @Test
    @DisplayName("Should find failed activities and detect any failure in process")
    void findFailedAndIsAnyFailed() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity1 = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        var activity2 = activityPersistence.run(ActivityTestData.createNotificationToSellerActivityExecution(process));
        activityPersistence.fail(activity2);

        // When
        var failedActivities = activityPersistence.findFailed(process.id());
        var anyFailed = activityPersistence.isAnyFailed(process.id());

        // Then
        assertThat(failedActivities).hasSize(1);
        assertThat(failedActivities.getFirst().id()).isEqualTo(activity2.id());
        assertThat(anyFailed).isTrue();

        // When & Then - new process without failures
        var processWithoutFailures = runOrderSubmittedProcess();
        activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(processWithoutFailures));
        assertThat(activityPersistence.findFailed(processWithoutFailures.id())).isEmpty();
        assertThat(activityPersistence.isAnyFailed(processWithoutFailures.id())).isFalse();
    }

    @Test
    @DisplayName("Should poll scheduled activities, update their state and respect limit")
    void pollWithScheduledActivities() {
        // Given
        var process = runOrderSubmittedProcess();
        activityPersistence.schedule(ActivityTestData.createNotificationToClientActivityExecution(process));
        activityPersistence.schedule(ActivityTestData.createNotificationToSellerActivityExecution(process));
        var topic = "notification";
        var processDefinitionKey = "order-submitted-process";

        // When
        var polledFirst = activityPersistence.poll(topic, processDefinitionKey, 1);

        // Then
        assertThat(polledFirst).hasSize(1);
        assertThat(polledFirst.getFirst().state()).isEqualTo(ActivityState.ACTIVE);
        assertThat(polledFirst.getFirst().startedAt()).isNotNull();
        assertThat(polledFirst.getFirst().updatedAt()).isNotNull();
        var persistedFirst = activityPersistence.findById(polledFirst.getFirst().id());
        assertThat(persistedFirst).isPresent();
        assertThat(persistedFirst.get().state()).isEqualTo(ActivityState.ACTIVE);

        // When & Then - poll remaining one, then empty
        var polledSecond = activityPersistence.poll(topic, processDefinitionKey, 2);
        assertThat(polledSecond).hasSize(1);
        var polledNone = activityPersistence.poll(topic, processDefinitionKey, 2);
        assertThat(polledNone).isEmpty();
    }

    @Test
    @DisplayName("Should delete only specified active activities and ignore unknown definition IDs")
    void deleteAllActiveWithEdgeCases() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity1 = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        var activity2 = activityPersistence.run(ActivityTestData.createNotificationToSellerActivityExecution(process));
        assertThat(activityPersistence.findActive(process.id())).hasSize(2);

        // When & Then - unknown definition ID does nothing
        activityPersistence.deleteAllActive(process.id(), List.of("Unknown_Definition"));
        assertThat(activityPersistence.findActive(process.id())).hasSize(2);

        // When & Then - delete one by its definition ID
        activityPersistence.deleteAllActive(process.id(), List.of(activity1.definitionId()));
        var remainingActivities = activityPersistence.findActive(process.id());
        assertThat(remainingActivities).hasSize(1);
        assertThat(remainingActivities.getFirst().definitionId()).isEqualTo(activity2.definitionId());

        // When & Then - delete the rest
        activityPersistence.deleteAllActive(process.id(), List.of(activity2.definitionId()));
        assertThat(activityPersistence.findActive(process.id())).isEmpty();
    }

    @Test
    @DisplayName("Should find only past timed-out activities and honor limit")
    void findTimedOutWithBoundariesAndLimit() {
        // Given
        var process = runOrderSubmittedProcess();
        var pastActivity = ActivityTestData.createNotificationToClientActivityExecution(process).toBuilder()
                .timeout(LocalDateTime.now().minusMinutes(2)).build();
        var exactlyNowActivity = ActivityTestData.createNotificationToSellerActivityExecution(process).toBuilder()
                .timeout(LocalDateTime.now()).build();
        var futureActivity = ActivityTestData.createNotificationToClientActivityExecution(process).toBuilder()
                .timeout(LocalDateTime.now().plusMinutes(2)).build();
        activityPersistence.run(pastActivity);
        activityPersistence.run(exactlyNowActivity);
        activityPersistence.run(futureActivity);

        // When
        var limitedResults = activityPersistence.findTimedOut(1);

        // Then
        assertThat(limitedResults).hasSize(1);
        assertThat(limitedResults.getFirst().timeout()).isBefore(LocalDateTime.now());

        // When & Then - fetch all
        var allResults = activityPersistence.findTimedOut(10);
        assertThat(allResults).allSatisfy(activity -> assertThat(activity.timeout()).isBefore(LocalDateTime.now()));
        assertThat(allResults.size()).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should return false when not all children under parent are completed")
    void isAllCompletedByParentWhenIncomplete() {
        // Given
        var process = runOrderFulfilledProcess();
        var childActivity = ActivityExecution.builder()
                .definitionId("ProcessOrderFulfillment")
                .process(process)
                .variables(List.of(VariableTestData.createOrderVariable(), VariableTestData.createClientVariable()))
                .retries(3)
                .build();
        var runningChild = activityPersistence.run(childActivity);
        var parentDefinitionId = "OrderFulfillment";

        // When
        var resultWhenIncomplete = activityPersistence.isAllCompleted(process.id(), parentDefinitionId);

        // Then
        assertThat(resultWhenIncomplete).isFalse();

        // When & Then - complete the child and verify becomes true
        activityPersistence.complete(runningChild);
        var resultWhenComplete = activityPersistence.isAllCompleted(process.id(), parentDefinitionId);
        assertThat(resultWhenComplete).isTrue();
    }

    @Test
    @DisplayName("Should find all active activities by process ID overloaded method")
    void findActiveByProcessId() {
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

}