package com.leorces.persistence.postgres;


import com.leorces.model.definition.ProcessDefinition;
import com.leorces.model.definition.ProcessDefinitionMetadata;
import com.leorces.model.pagination.Pageable;
import com.leorces.persistence.postgres.cache.DefinitionCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.leorces.persistence.postgres.utils.ProcessDefinitionTestData.createOrderFulfillmentProcessDefinition;
import static com.leorces.persistence.postgres.utils.ProcessDefinitionTestData.createOrderSubmittedProcessDefinition;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Definition Persistence Integration Tests")
class DefinitionPersistenceIT extends RepositoryIT {

    @Autowired
    private DefinitionCache cache;

    @Test
    @DisplayName("Should save multiple process definitions with generated IDs and timestamps")
    void save() {
        // Given
        var orderFulfillment = createOrderFulfillmentProcessDefinition();
        var orderSubmitted = createOrderSubmittedProcessDefinition();

        // When
        var result = definitionPersistence.save(List.of(orderFulfillment, orderSubmitted));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProcessDefinition::id).allSatisfy(id -> assertThat(id).isNotNull());
        assertThat(result).extracting(ProcessDefinition::createdAt).allSatisfy(createdAt -> assertThat(createdAt).isNotNull());
        assertThat(result).extracting(ProcessDefinition::updatedAt).allSatisfy(updatedAt -> assertThat(updatedAt).isNotNull());

        // Verify the activities, messages, and errors are preserved
        var savedOrderFulfillment = result.getFirst();
        var savedOrderSubmitted = result.getLast();

        // Verify OrderFulfillment ProcessDefinition fields
        assertThat(savedOrderFulfillment.key()).isEqualTo(orderFulfillment.key());
        assertThat(savedOrderFulfillment.name()).isEqualTo(orderFulfillment.name());
        assertThat(savedOrderFulfillment.version()).isEqualTo(orderFulfillment.version());

        // Verify OrderFulfillment metadata
        assertThat(savedOrderFulfillment.metadata()).isNotNull();
        assertThat(savedOrderFulfillment.metadata().schema()).isEqualTo(orderFulfillment.metadata().schema());
        assertThat(savedOrderFulfillment.metadata().origin()).isEqualTo(orderFulfillment.metadata().origin());
        assertThat(savedOrderFulfillment.metadata().deployment()).isEqualTo(orderFulfillment.metadata().deployment());

        // Verify OrderFulfillment messages content
        assertThat(savedOrderFulfillment.messages()).hasSize(1);
        assertThat(savedOrderFulfillment.messages()).containsExactlyElementsOf(orderFulfillment.messages());

        // Verify OrderFulfillment errors
        assertThat(savedOrderFulfillment.errors()).isEmpty();
        assertThat(savedOrderFulfillment.errors()).hasSameSizeAs(orderFulfillment.errors());

        // Verify OrderFulfillment activities
        assertThat(savedOrderFulfillment.activities()).hasSize(9);
        assertThat(savedOrderFulfillment.activities()).hasSameSizeAs(orderFulfillment.activities());

        // Verify specific activity details for OrderFulfillment
        var startEvent = savedOrderFulfillment.activities().stream()
                .filter(activity -> "OrderFulfillmentProcessStartEvent".equals(activity.id()))
                .findFirst().orElseThrow();
        assertThat(startEvent.name()).isEmpty();
        assertThat(startEvent.parentId()).isNull();
        assertThat(startEvent.incoming()).isEmpty();
        assertThat(startEvent.outgoing()).containsExactly("OrderFulfillmentNotification");

        var externalTask = savedOrderFulfillment.activities().stream()
                .filter(activity -> "OrderFulfillmentNotification".equals(activity.id()))
                .findFirst().orElseThrow();
        assertThat(externalTask.name()).isEqualTo("Order fulfillment notification");
        assertThat(externalTask.parentId()).isNull();
        assertThat(externalTask.incoming()).containsExactly("OrderFulfillmentProcessStartEvent");
        assertThat(externalTask.outgoing()).containsExactly("WaitOrderFulfillment");
        assertThat(externalTask.inputs()).containsKey("message");
        assertThat(externalTask.outputs()).isEmpty();

        // Verify OrderSubmitted ProcessDefinition fields
        assertThat(savedOrderSubmitted.key()).isEqualTo(orderSubmitted.key());
        assertThat(savedOrderSubmitted.name()).isEqualTo(orderSubmitted.name());
        assertThat(savedOrderSubmitted.version()).isEqualTo(orderSubmitted.version());

        // Verify OrderSubmitted metadata
        assertThat(savedOrderSubmitted.metadata()).isNotNull();
        assertThat(savedOrderSubmitted.metadata().schema()).isEqualTo(orderSubmitted.metadata().schema());
        assertThat(savedOrderSubmitted.metadata().origin()).isEqualTo(orderSubmitted.metadata().origin());
        assertThat(savedOrderSubmitted.metadata().deployment()).isEqualTo(orderSubmitted.metadata().deployment());

        // Verify OrderSubmitted messages and errors
        assertThat(savedOrderSubmitted.messages()).isEmpty();
        assertThat(savedOrderSubmitted.messages()).hasSameSizeAs(orderSubmitted.messages());
        assertThat(savedOrderSubmitted.errors()).isEmpty();
        assertThat(savedOrderSubmitted.errors()).hasSameSizeAs(orderSubmitted.errors());

        // Verify OrderSubmitted activities
        assertThat(savedOrderSubmitted.activities()).hasSize(6);
        assertThat(savedOrderSubmitted.activities()).hasSameSizeAs(orderSubmitted.activities());

        // Verify specific activity details for OrderSubmitted
        var submittedStartEvent = savedOrderSubmitted.activities().stream()
                .filter(activity -> "OrderSubmittedProcessStartEvent".equals(activity.id()))
                .findFirst().orElseThrow();
        assertThat(submittedStartEvent.name()).isEmpty();
        assertThat(submittedStartEvent.parentId()).isNull();
        assertThat(submittedStartEvent.incoming()).isEmpty();
        assertThat(submittedStartEvent.outgoing()).containsExactly("Gateway_0t354xy");

        var clientNotification = savedOrderSubmitted.activities().stream()
                .filter(activity -> "NotificationToClient".equals(activity.id()))
                .findFirst().orElseThrow();
        assertThat(clientNotification.name()).isEqualTo("Notification to client");
        assertThat(clientNotification.parentId()).isNull();
        assertThat(clientNotification.incoming()).containsExactly("Gateway_0t354xy");
        assertThat(clientNotification.outgoing()).containsExactly("Gateway_0yprn2c");
        assertThat(clientNotification.inputs()).containsKey("message");
        assertThat(clientNotification.outputs()).isEmpty();
    }

    @Test
    @DisplayName("Should suspend and resume definition by ID")
    void suspendAndResumeById() {
        // Given
        var definition = definitionPersistence.save(List.of(createOrderSubmittedProcessDefinition())).getFirst();
        var id = definition.id();

        // When
        cache.invalidateAll();
        definitionPersistence.suspendById(id);
        var suspendedDefinition = definitionPersistence.findById(id).orElseThrow();

        // Then
        assertThat(suspendedDefinition.suspended()).isTrue();

        // When
        definitionPersistence.resumeById(id);
        cache.invalidateAll();
        var resumedDefinition = definitionPersistence.findById(id).orElseThrow();

        // Then
        assertThat(resumedDefinition.suspended()).isFalse();
    }

    @Test
    @DisplayName("Should suspend and resume definition by key")
    void suspendAndResumeByKey() {
        // Given
        var definition = definitionPersistence.save(List.of(createOrderSubmittedProcessDefinition())).getFirst();
        var key = definition.key();
        var id = definition.id();

        // When
        cache.invalidateAll();
        definitionPersistence.suspendByKey(key);
        var suspendedDefinition = definitionPersistence.findById(id).orElseThrow();

        // Then
        assertThat(suspendedDefinition.suspended()).isTrue();

        // When
        definitionPersistence.resumeByKey(key);
        cache.invalidateAll();
        var resumedDefinition = definitionPersistence.findById(id).orElseThrow();

        // Then
        assertThat(resumedDefinition.suspended()).isFalse();
    }

    @Test
    @DisplayName("Should create new version when schema changes")
    void saveWithSchemaChange() {
        // Given
        var v1 = createOrderSubmittedProcessDefinition();
        definitionPersistence.save(List.of(v1));

        var v2 = v1.toBuilder()
                .metadata(ProcessDefinitionMetadata.builder()
                        .schema("new-schema")
                        .origin(v1.metadata().origin())
                        .deployment(v1.metadata().deployment())
                        .build())
                .build();

        // When
        var result = definitionPersistence.save(List.of(v2));

        // Then
        assertThat(result).hasSize(1);
        var savedV2 = result.getFirst();
        assertThat(savedV2.version()).isEqualTo(2);
        assertThat(savedV2.metadata().schema()).isEqualTo("new-schema");
    }

    @Test
    @DisplayName("Should find process definition by ID when it exists")
    void findById() {
        // Given
        var orderFulfillment = createOrderFulfillmentProcessDefinition();
        var savedDefinitions = definitionPersistence.save(List.of(orderFulfillment));
        var savedDefinition = savedDefinitions.getFirst();

        // When
        var result = definitionPersistence.findById(savedDefinition.id());

        // Then
        assertThat(result).isPresent();
        var foundDefinition = result.get();

        // Verify ProcessDefinition fields
        assertThat(foundDefinition.id()).isNotNull();
        assertThat(foundDefinition.key()).isEqualTo(orderFulfillment.key());
        assertThat(foundDefinition.name()).isEqualTo(orderFulfillment.name());
        assertThat(foundDefinition.version()).isEqualTo(orderFulfillment.version());
        assertThat(foundDefinition.createdAt()).isNotNull();
        assertThat(foundDefinition.updatedAt()).isNotNull();

        // Verify metadata fields
        assertThat(foundDefinition.metadata()).isNotNull();
        assertThat(foundDefinition.metadata().schema()).isEqualTo(orderFulfillment.metadata().schema());
        assertThat(foundDefinition.metadata().origin()).isEqualTo(orderFulfillment.metadata().origin());
        assertThat(foundDefinition.metadata().deployment()).isEqualTo(orderFulfillment.metadata().deployment());

        // Verify messages content
        assertThat(foundDefinition.messages()).hasSize(1);
        assertThat(foundDefinition.messages()).containsExactlyElementsOf(orderFulfillment.messages());
        assertThat(foundDefinition.messages()).containsExactly("OrderFulfillmentFinishedMessage");

        // Verify errors
        assertThat(foundDefinition.errors()).isEmpty();
        assertThat(foundDefinition.errors()).hasSameSizeAs(orderFulfillment.errors());

        // Verify activities collection
        assertThat(foundDefinition.activities()).hasSize(9);
        assertThat(foundDefinition.activities()).hasSameSizeAs(orderFulfillment.activities());

        // Verify specific activity fields
        var startEvent = foundDefinition.activities().stream()
                .filter(activity -> "OrderFulfillmentProcessStartEvent".equals(activity.id()))
                .findFirst().orElseThrow();
        assertThat(startEvent.name()).isEmpty();
        assertThat(startEvent.parentId()).isNull();
        assertThat(startEvent.incoming()).isEmpty();
        assertThat(startEvent.outgoing()).containsExactly("OrderFulfillmentNotification");
        assertThat(startEvent.inputs()).isEmpty();
        assertThat(startEvent.outputs()).isEmpty();

        var eventSubprocess = foundDefinition.activities().stream()
                .filter(activity -> "OrderFulfillment".equals(activity.id()))
                .findFirst().orElseThrow();
        assertThat(eventSubprocess.name()).isEqualTo("Order fulfillment");
        assertThat(eventSubprocess.parentId()).isNull();
        assertThat(eventSubprocess.incoming()).isEmpty();
        assertThat(eventSubprocess.outgoing()).isEmpty();

        var messageStartEvent = foundDefinition.activities().stream()
                .filter(activity -> "OrderFulfillmentStartEvent".equals(activity.id()))
                .findFirst().orElseThrow();
        assertThat(messageStartEvent.name()).isEmpty();
        assertThat(messageStartEvent.parentId()).isEqualTo("OrderFulfillment");
        assertThat(messageStartEvent.incoming()).isEmpty();
        assertThat(messageStartEvent.outgoing()).containsExactly("ProcessOrderFulfillment");
    }

    @Test
    @DisplayName("Should return empty optional when process definition ID does not exist")
    void findById_NotFound() {
        // Given
        var nonExistentId = "non-existent-id";

        // When
        var result = definitionPersistence.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find latest version of process definition by key")
    void findLatestByKey() {
        // Given
        var orderFulfillment = createOrderFulfillmentProcessDefinition();
        var savedDefinitions = definitionPersistence.save(List.of(orderFulfillment));
        var savedDefinition = savedDefinitions.getFirst();

        // When
        var result = definitionPersistence.findLatestByKey(savedDefinition.key());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().key()).isEqualTo(orderFulfillment.key());
        assertThat(result.get().version()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return empty optional when process definition key does not exist")
    void findLatestByKey_NotFound() {
        // Given
        var nonExistentKey = "non-existent-key";

        // When
        var result = definitionPersistence.findLatestByKey(nonExistentKey);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return all process definitions with pagination")
    void findAll() {
        // Given
        var orderFulfillment = createOrderFulfillmentProcessDefinition();
        var orderSubmitted = createOrderSubmittedProcessDefinition();
        definitionPersistence.save(List.of(orderFulfillment, orderSubmitted));
        var pageable = new Pageable(0, 10);

        // When
        var result = definitionPersistence.findAll(pageable);

        // Then
        assertThat(result.data()).hasSize(2);
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.data()).extracting(ProcessDefinition::key)
                .containsExactlyInAnyOrder("order-fulfillment-process", "order-submitted-process");
    }

    @Test
    @DisplayName("Should respect pagination limit when finding all process definitions")
    void findAll_WithPagination() {
        // Given
        var orderFulfillment = createOrderFulfillmentProcessDefinition();
        var orderSubmitted = createOrderSubmittedProcessDefinition();
        definitionPersistence.save(List.of(orderFulfillment, orderSubmitted));
        var pageable = new Pageable(0, 1);

        // When
        var result = definitionPersistence.findAll(pageable);

        // Then
        assertThat(result.data()).hasSize(1);
        assertThat(result.total()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return empty result when no process definitions exist")
    void findAll_Empty() {
        // Given
        var pageable = new Pageable(0, 10);

        // When
        var result = definitionPersistence.findAll(pageable);

        // Then
        assertThat(result.data()).isEmpty();
        assertThat(result.total()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should save single process definition in list with generated ID and version 1")
    void testSave() {
        // Given
        var orderFulfillment = createOrderFulfillmentProcessDefinition();

        // When
        var result = definitionPersistence.save(List.of(orderFulfillment));

        // Then
        assertThat(result).hasSize(1);
        var savedDefinition = result.getFirst();
        assertThat(savedDefinition.id()).isNotNull();
        assertThat(savedDefinition.version()).isEqualTo(1);
        assertThat(savedDefinition.key()).isEqualTo(orderFulfillment.key());
        assertThat(savedDefinition.name()).isEqualTo(orderFulfillment.name());
        assertThat(savedDefinition.createdAt()).isNotNull();
        assertThat(savedDefinition.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find process definition by key and version when it exists")
    void findByKeyAndVersion() {
        // Given
        var orderFulfillment = createOrderFulfillmentProcessDefinition();
        var savedDefinitions = definitionPersistence.save(List.of(orderFulfillment));
        var savedDefinition = savedDefinitions.getFirst();

        // When
        var result = definitionPersistence.findByKeyAndVersion(savedDefinition.key(), savedDefinition.version());

        // Then
        assertThat(result).isPresent();

        var foundDefinition = result.get();

        // Verify ProcessDefinition fields
        assertThat(foundDefinition.id()).isNotNull();
        assertThat(foundDefinition.key()).isEqualTo(orderFulfillment.key());
        assertThat(foundDefinition.name()).isEqualTo(orderFulfillment.name());
        assertThat(foundDefinition.version()).isEqualTo(1);
        assertThat(foundDefinition.createdAt()).isNotNull();
        assertThat(foundDefinition.updatedAt()).isNotNull();

        // Verify metadata fields
        assertThat(foundDefinition.metadata()).isNotNull();
        assertThat(foundDefinition.metadata().schema()).isEqualTo(orderFulfillment.metadata().schema());
        assertThat(foundDefinition.metadata().origin()).isEqualTo(orderFulfillment.metadata().origin());
        assertThat(foundDefinition.metadata().deployment()).isEqualTo(orderFulfillment.metadata().deployment());

        // Verify activities collection
        assertThat(foundDefinition.activities()).hasSize(9);
        assertThat(foundDefinition.activities()).hasSameSizeAs(orderFulfillment.activities());
    }

    @Test
    @DisplayName("Should return empty optional when process definition key and version combination does not exist")
    void findByKeyAndVersion_NotFound() {
        // Given
        var orderFulfillment = createOrderFulfillmentProcessDefinition();
        definitionPersistence.save(List.of(orderFulfillment));

        // When & Then - test non-existent key
        var resultWithWrongKey = definitionPersistence.findByKeyAndVersion("non-existent-key", 1);
        assertThat(resultWithWrongKey).isEmpty();

        // When & Then - test non-existent version
        var resultWithWrongVersion = definitionPersistence.findByKeyAndVersion("order-fulfillment-process", 999);
        assertThat(resultWithWrongVersion).isEmpty();
    }

    @Test
    @DisplayName("Should handle concurrent definition saves when 10 threads attempt to save the same definition")
    void concurrentDefinitionSave() throws InterruptedException {
        // Given
        var numberOfThreads = 10;
        var latch = new CountDownLatch(numberOfThreads);
        var executor = Executors.newFixedThreadPool(numberOfThreads);
        var definition = createOrderFulfillmentProcessDefinition();

        // When
        var futuresList = new java.util.ArrayList<CompletableFuture<List<ProcessDefinition>>>();
        for (int i = 0; i < numberOfThreads; i++) {
            var future = CompletableFuture.supplyAsync(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    return definitionPersistence.save(List.of(definition));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return List.<ProcessDefinition>of();
                } catch (Exception e) {
                    // Handle transaction rollback or other exceptions gracefully
                    // This is expected in concurrent scenarios due to unique constraint
                    return List.<ProcessDefinition>of();
                }
            }, executor);
            futuresList.add(future);
        }

        CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
        var terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
        assertThat(terminated).isTrue();

        // Then
        var allDefinitions = definitionPersistence.findAll(new Pageable(0, 20));

        // Due to unique constraint on key+version in database and proper concurrency handling,
        // only one definition should be created despite multiple concurrent attempts
        assertThat(allDefinitions.data()).hasSize(1);
        assertThat(allDefinitions.total()).isEqualTo(1);

        // The single saved definition should have the expected properties
        var savedDefinition = allDefinitions.data().getFirst();
        assertThat(savedDefinition.key()).isEqualTo(definition.key());
        assertThat(savedDefinition.version()).isEqualTo(1);
        assertThat(savedDefinition.name()).isEqualTo(definition.name());
        assertThat(savedDefinition.id()).isNotNull();
        assertThat(savedDefinition.createdAt()).isNotNull();
        assertThat(savedDefinition.updatedAt()).isNotNull();

        // Verify all futures completed successfully and returned the same definition
        for (var future : futuresList) {
            var result = future.join();
            if (!result.isEmpty()) {
                var resultDefinition = result.getFirst();
                assertThat(resultDefinition.key()).isEqualTo(definition.key());
                assertThat(resultDefinition.version()).isEqualTo(1);
            }
        }
    }

}