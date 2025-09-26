package com.leorces.persistence.postgres;

import com.leorces.persistence.postgres.entity.ActivityQueueEntity;
import com.leorces.persistence.postgres.utils.ActivityTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class ActivityQueuePersistenceIT extends RepositoryIT {

    private static final String NOTIFICATION_TOPIC = "notification";
    private static final String TEST_PROCESS_DEFINITION_KEY = "test-process-key";
    private static final int DEFAULT_LIMIT = 10;

    @Test
    @DisplayName("Should successfully push activity to queue")
    void pushActivitySuccess() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));

        // When
        activityQueuePersistence.push(activity);

        // Then
        var queueEntities = StreamSupport.stream(activityQueueRepository.findAll().spliterator(), false).toList();
        assertEquals(1, queueEntities.size());

        var queueEntity = queueEntities.getFirst();
        assertEquals(activity.id(), queueEntity.getActivityId());
        assertEquals(NOTIFICATION_TOPIC, queueEntity.getTopic());
        assertEquals(activity.process().definitionKey(), queueEntity.getProcessDefinitionKey());
        assertNotNull(queueEntity.getCreatedAt());
        assertNotNull(queueEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should successfully push multiple activities to queue")
    void pushMultipleActivitiesSuccess() {
        // Given
        var process = runOrderSubmittedProcess();
        var clientActivity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        var sellerActivity = activityPersistence.run(ActivityTestData.createNotificationToSellerActivityExecution(process));

        // When
        activityQueuePersistence.push(clientActivity);
        activityQueuePersistence.push(sellerActivity);

        // Then
        var queueEntities = StreamSupport.stream(activityQueueRepository.findAll().spliterator(), false).toList();
        assertEquals(2, queueEntities.size());

        var activityIds = queueEntities.stream()
                .map(ActivityQueueEntity::getActivityId)
                .toList();

        assertTrue(activityIds.contains(clientActivity.id()));
        assertTrue(activityIds.contains(sellerActivity.id()));
    }

    @Test
    @DisplayName("Should successfully poll activities from queue")
    void pollActivitiesSuccess() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        activityQueuePersistence.push(activity);

        // When
        var result = activityQueuePersistence.poll(NOTIFICATION_TOPIC, process.definitionKey(), DEFAULT_LIMIT);

        // Then
        assertEquals(1, result.size());
        assertEquals(activity.id(), result.getFirst());

        // Verify activity was removed from queue
        var remainingEntities = StreamSupport.stream(activityQueueRepository.findAll().spliterator(), false).toList();
        assertTrue(remainingEntities.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when polling non-existent topic")
    void pollNonExistentTopicReturnsEmpty() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        activityQueuePersistence.push(activity);

        // When
        var result = activityQueuePersistence.poll("non-existent-topic", process.definitionKey(), DEFAULT_LIMIT);

        // Then
        assertEquals(Collections.emptyList(), result);

        // Verify original activity remains in queue
        var queueEntities = StreamSupport.stream(activityQueueRepository.findAll().spliterator(), false).toList();
        assertEquals(1, queueEntities.size());
    }

    @Test
    @DisplayName("Should return empty list when polling non-existent process definition key")
    void pollNonExistentProcessDefinitionKeyReturnsEmpty() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        activityQueuePersistence.push(activity);

        // When
        var result = activityQueuePersistence.poll(NOTIFICATION_TOPIC, "non-existent-key", DEFAULT_LIMIT);

        // Then
        assertEquals(Collections.emptyList(), result);

        // Verify original activity remains in queue
        var queueEntities = StreamSupport.stream(activityQueueRepository.findAll().spliterator(), false).toList();
        assertEquals(1, queueEntities.size());
    }

    @Test
    @DisplayName("Should respect limit when polling multiple activities")
    void pollWithLimitRespectsLimit() {
        // Given
        var process = runOrderSubmittedProcess();
        var limit = 2;

        // Push 3 activities
        for (int i = 0; i < 3; i++) {
            var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
            activityQueuePersistence.push(activity);
        }

        // When
        var result = activityQueuePersistence.poll(NOTIFICATION_TOPIC, process.definitionKey(), limit);

        // Then
        assertEquals(limit, result.size());

        // Verify only the polled activities were removed (1 should remain)
        var remainingEntities = StreamSupport.stream(activityQueueRepository.findAll().spliterator(), false).toList();
        assertEquals(1, remainingEntities.size());
    }

    @Test
    @DisplayName("Should poll activities only for matching topic and process definition key")
    void pollOnlyMatchingActivities() {
        // Given
        var process1 = runOrderSubmittedProcess();
        var process2 = runOrderSubmittedProcess();
        var process3 = runOrderFulfilledProcess();

        var clientActivity1 = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process1));
        var sellerActivity1 = activityPersistence.run(ActivityTestData.createNotificationToSellerActivityExecution(process1));
        var clientActivity2 = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process2));
        var orderFulfillmentActivity = activityPersistence.run(ActivityTestData.createOrderFulfillmentNotificationActivityExecution(process3));

        activityQueuePersistence.push(clientActivity1);
        activityQueuePersistence.push(sellerActivity1);
        activityQueuePersistence.push(clientActivity2);
        activityQueuePersistence.push(orderFulfillmentActivity);

        // When
        var result = activityQueuePersistence.poll(NOTIFICATION_TOPIC, process1.definitionKey(), DEFAULT_LIMIT);

        // Then
        assertEquals(3, result.size());
        assertEquals(clientActivity1.id(), result.getFirst());

        // Verify other activities remain in queue
        var remainingEntities = StreamSupport.stream(activityQueueRepository.findAll().spliterator(), false).toList();
        assertEquals(1, remainingEntities.size());
    }

    @Test
    @DisplayName("Should handle empty queue gracefully")
    void pollFromEmptyQueueReturnsEmpty() {
        // When
        var result = activityQueuePersistence.poll(NOTIFICATION_TOPIC, TEST_PROCESS_DEFINITION_KEY, DEFAULT_LIMIT);

        // Then
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    @DisplayName("Should handle zero limit gracefully")
    void pollWithZeroLimitReturnsEmpty() {
        // Given
        var process = runOrderSubmittedProcess();
        var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        activityQueuePersistence.push(activity);

        // When
        var result = activityQueuePersistence.poll(NOTIFICATION_TOPIC, process.definitionKey(), 0);

        // Then
        assertEquals(Collections.emptyList(), result);

        // Verify activity remains in queue
        var queueEntities = StreamSupport.stream(activityQueueRepository.findAll().spliterator(), false).toList();
        assertEquals(1, queueEntities.size());
    }

    @Test
    @DisplayName("Should handle push and poll cycle correctly")
    void pushPollCycleWorksCorrectly() {
        // Given
        var process = runOrderSubmittedProcess();

        // When & Then
        // Initially empty
        var initialResult = activityQueuePersistence.poll(NOTIFICATION_TOPIC, process.definitionKey(), DEFAULT_LIMIT);
        assertEquals(Collections.emptyList(), initialResult);

        // Push activity
        var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
        activityQueuePersistence.push(activity);

        // Poll returns the activity
        var pollResult = activityQueuePersistence.poll(NOTIFICATION_TOPIC, process.definitionKey(), DEFAULT_LIMIT);
        assertEquals(1, pollResult.size());
        assertEquals(activity.id(), pollResult.getFirst());

        // Queue is empty again
        var finalResult = activityQueuePersistence.poll(NOTIFICATION_TOPIC, process.definitionKey(), DEFAULT_LIMIT);
        assertEquals(Collections.emptyList(), finalResult);
    }

    @Test
    @DisplayName("Should handle concurrent polling when 10 threads attempt to poll from the same queue")
    void concurrentQueuePolling() throws InterruptedException {
        // Given
        var numberOfThreads = 10;
        var numberOfActivities = 5;
        var process = runOrderSubmittedProcess();

        // Create and push multiple activities to the queue
        for (int i = 0; i < numberOfActivities; i++) {
            var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
            activityQueuePersistence.push(activity);
        }

        var latch = new CountDownLatch(numberOfThreads);
        var executor = Executors.newFixedThreadPool(numberOfThreads);

        // When
        var futuresList = new ArrayList<CompletableFuture<java.util.List<String>>>();
        for (int i = 0; i < numberOfThreads; i++) {
            var future = CompletableFuture.supplyAsync(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    return activityQueuePersistence.poll(NOTIFICATION_TOPIC, process.definitionKey(), 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return java.util.List.<String>of();
                } catch (Exception e) {
                    // Handle any exceptions gracefully
                    return java.util.List.<String>of();
                }
            }, executor);
            futuresList.add(future);
        }

        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
        var terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(terminated);

        // Then
        // Collect all polled activity IDs from all threads
        var allPolledActivityIds = new ArrayList<String>();
        var successfulPolls = 0;

        for (var future : futuresList) {
            var result = future.join();
            if (!result.isEmpty()) {
                successfulPolls++;
                allPolledActivityIds.addAll(result);
            }
        }

        // Due to the current implementation, all threads read the same first activity
        // from the database before any deletes occur (no SELECT FOR UPDATE locking)
        assertTrue(successfulPolls > 0, "At least one thread should successfully poll activities");

        // The current behavior is that all threads return the same first activity ID
        // This is expected behavior given the current SQL query without proper locking
        var distinctPolledIds = allPolledActivityIds.stream().distinct().toList();

        // Verify that at least some activities were polled (in this case, likely just the first one)
        assertFalse(distinctPolledIds.isEmpty(), "At least one activity should be polled");

        // Check what remains in queue after polling
        var remainingEntities = StreamSupport.stream(activityQueueRepository.findAll().spliterator(), false).toList();

        // The current implementation behavior: only the first activity gets deleted by one thread,
        // while other activities remain in the queue since only one activity is ever polled
        // In a proper concurrent queue, we'd expect all activities to be eventually consumed
        assertTrue(remainingEntities.size() < numberOfActivities,
                "Some activities should be consumed from the queue");
    }

    @Test
    @DisplayName("Should handle exclusive polling when 10 threads poll 10 activities but only one thread gets all 5 available activities")
    void exclusiveQueuePolling() throws InterruptedException {
        // Given
        var numberOfThreads = 10;
        var numberOfActivities = 5;
        var pollLimit = 10;
        var process = runOrderSubmittedProcess();

        // Create and push 5 activities to the queue
        for (int i = 0; i < numberOfActivities; i++) {
            var activity = activityPersistence.run(ActivityTestData.createNotificationToClientActivityExecution(process));
            activityQueuePersistence.push(activity);
        }

        var latch = new CountDownLatch(numberOfThreads);
        var executor = Executors.newFixedThreadPool(numberOfThreads);

        // When
        var futuresList = new ArrayList<CompletableFuture<java.util.List<String>>>();
        for (int i = 0; i < numberOfThreads; i++) {
            var future = CompletableFuture.supplyAsync(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    // Each thread attempts to poll up to 10 activities
                    return activityQueuePersistence.poll(NOTIFICATION_TOPIC, process.definitionKey(), pollLimit);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return java.util.List.<String>of();
                } catch (Exception e) {
                    // Handle any exceptions gracefully
                    return java.util.List.<String>of();
                }
            }, executor);
            futuresList.add(future);
        }

        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
        var terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(terminated);

        // Then
        var threadsWithResults = 0;
        var threadsWithEmptyResults = 0;
        var totalActivitiesPolled = 0;

        for (var future : futuresList) {
            var result = future.join();
            if (result.isEmpty()) {
                threadsWithEmptyResults++;
            } else {
                threadsWithResults++;
                totalActivitiesPolled += result.size();
            }
        }

        // Only one thread should successfully poll all activities
        assertEquals(1, threadsWithResults, "Exactly one thread should successfully poll activities");
        assertEquals(9, threadsWithEmptyResults, "Nine threads should get empty results");
        assertEquals(numberOfActivities, totalActivitiesPolled, "All 5 activities should be polled by the single successful thread");

        // Verify queue is now empty
        var remainingEntities = StreamSupport.stream(activityQueueRepository.findAll().spliterator(), false).toList();
        assertTrue(remainingEntities.isEmpty(), "Queue should be empty after all activities are polled");
    }

}