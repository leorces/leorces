package com.leorces.engine.activity;

import com.leorces.engine.activity.behaviour.ActivityBehaviorResolver;
import com.leorces.engine.activity.behaviour.CancellableActivityBehaviour;
import com.leorces.engine.event.activity.cancel.CancelActivitiesByProcessIdEvent;
import com.leorces.engine.event.activity.cancel.CancelActivitiesEvent;
import com.leorces.engine.event.activity.terminate.TerminateActivitiesByProcessIdEvent;
import com.leorces.engine.event.activity.terminate.TerminateActivityByIdAsync;
import com.leorces.engine.event.activity.terminate.TerminateActivityEvent;
import com.leorces.engine.service.TaskExecutorService;
import com.leorces.model.definition.activity.ActivityType;
import com.leorces.model.runtime.activity.ActivityExecution;
import com.leorces.model.runtime.activity.ActivityState;
import com.leorces.persistence.ActivityPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityCancelService Tests")
class ActivityCancelServiceTest {

    private static final String ACTIVITY_ID = "test-activity-id";
    private static final String PROCESS_ID = "test-process-id";

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private ActivityBehaviorResolver behaviorResolver;

    @Mock
    private ActivityFactory activityFactory;

    @Mock
    private TaskExecutorService taskExecutor;

    @Mock
    private CancellableActivityBehaviour cancellableBehaviour;

    private ActivityCancelService activityCancelService;

    @BeforeEach
    void setUp() {
        activityCancelService = new ActivityCancelService(
                activityPersistence,
                behaviorResolver,
                activityFactory,
                taskExecutor
        );
    }

    @Test
    @DisplayName("Should handle cancel activities event successfully")
    void shouldHandleCancelActivitiesEventSuccessfully() {
        // Given
        var activities = List.of(createActivityExecution());
        var event = new CancelActivitiesEvent(activities);

        when(behaviorResolver.resolveCancellableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(cancellableBehaviour));
        when(taskExecutor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run(); // Execute synchronously for testing
            return CompletableFuture.completedFuture(null);
        });

        // When
        activityCancelService.handleCancel(event);

        // Then
        verify(behaviorResolver).resolveCancellableStrategy(ActivityType.EXTERNAL_TASK);
        verify(cancellableBehaviour).cancel(any(ActivityExecution.class));
        verify(taskExecutor).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Should handle cancel activities by process ID event successfully")
    void shouldHandleCancelActivitiesByProcessIdEventSuccessfully() {
        // Given
        var activities = List.of(createActivityExecution());
        var event = new CancelActivitiesByProcessIdEvent(PROCESS_ID);

        when(activityPersistence.findActive(PROCESS_ID)).thenReturn(activities);
        when(behaviorResolver.resolveCancellableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(cancellableBehaviour));
        when(taskExecutor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return CompletableFuture.completedFuture(null);
        });

        // When
        activityCancelService.handleCancel(event);

        // Then
        verify(activityPersistence).findActive(PROCESS_ID);
        verify(behaviorResolver).resolveCancellableStrategy(ActivityType.EXTERNAL_TASK);
        verify(cancellableBehaviour).cancel(any(ActivityExecution.class));
        verify(taskExecutor).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Should handle terminate activities by process ID event successfully")
    void shouldHandleTerminateActivitiesByProcessIdEventSuccessfully() {
        // Given
        var activities = List.of(createActivityExecution());
        var event = new TerminateActivitiesByProcessIdEvent(PROCESS_ID);

        when(activityPersistence.findActive(PROCESS_ID)).thenReturn(activities);
        when(behaviorResolver.resolveCancellableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(cancellableBehaviour));
        when(taskExecutor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return CompletableFuture.completedFuture(null);
        });

        // When
        activityCancelService.handleTerminate(event);

        // Then
        verify(activityPersistence).findActive(PROCESS_ID);
        verify(behaviorResolver).resolveCancellableStrategy(ActivityType.EXTERNAL_TASK);
        verify(cancellableBehaviour).terminate(any(ActivityExecution.class));
        verify(taskExecutor).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Should handle terminate activity event successfully")
    void shouldHandleTerminateActivityEventSuccessfully() {
        // Given
        var activity = createActivityExecution();
        var event = new TerminateActivityEvent(activity);

        when(behaviorResolver.resolveCancellableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(cancellableBehaviour));

        // When
        activityCancelService.handleTerminate(event);

        // Then
        verify(behaviorResolver).resolveCancellableStrategy(ActivityType.EXTERNAL_TASK);
        verify(cancellableBehaviour).terminate(activity);
        verify(taskExecutor, never()).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Should handle terminate activity by ID async event successfully")
    void shouldHandleTerminateActivityByIdAsyncEventSuccessfully() {
        // Given
        var activity = createActivityExecution();
        var event = new TerminateActivityByIdAsync(ACTIVITY_ID);

        when(activityFactory.getById(ACTIVITY_ID)).thenReturn(activity);
        when(behaviorResolver.resolveCancellableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(cancellableBehaviour));

        // When
        activityCancelService.handleTerminate(event);

        // Then
        verify(activityFactory).getById(ACTIVITY_ID);
        verify(behaviorResolver).resolveCancellableStrategy(ActivityType.EXTERNAL_TASK);
        verify(cancellableBehaviour).terminate(activity);
        verify(taskExecutor, never()).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Should handle empty activities list gracefully")
    void shouldHandleEmptyActivitiesListGracefully() {
        // Given
        var emptyActivities = List.<ActivityExecution>of();
        var event = new CancelActivitiesEvent(emptyActivities);

        // When
        activityCancelService.handleCancel(event);

        // Then
        verify(behaviorResolver, never()).resolveCancellableStrategy(any());
        verify(cancellableBehaviour, never()).cancel(any());
        verify(taskExecutor, never()).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Should handle multiple activities cancellation")
    void shouldHandleMultipleActivitiesCancellation() {
        // Given
        var activity1 = createActivityExecution();
        var activity2 = createActivityExecution();
        var activities = List.of(activity1, activity2);
        var event = new CancelActivitiesEvent(activities);

        when(behaviorResolver.resolveCancellableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(cancellableBehaviour));
        when(taskExecutor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return CompletableFuture.completedFuture(null);
        });

        // When
        activityCancelService.handleCancel(event);

        // Then
        verify(behaviorResolver, times(2)).resolveCancellableStrategy(ActivityType.EXTERNAL_TASK);
        verify(cancellableBehaviour, times(2)).cancel(any(ActivityExecution.class));
        verify(taskExecutor, times(2)).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Should handle multiple activities termination")
    void shouldHandleMultipleActivitiesTermination() {
        // Given
        var activity1 = createActivityExecution();
        var activity2 = createActivityExecution();
        var activities = List.of(activity1, activity2);
        var event = new TerminateActivitiesByProcessIdEvent(PROCESS_ID);

        when(activityPersistence.findActive(PROCESS_ID)).thenReturn(activities);
        when(behaviorResolver.resolveCancellableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.of(cancellableBehaviour));
        when(taskExecutor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return CompletableFuture.completedFuture(null);
        });

        // When
        activityCancelService.handleTerminate(event);

        // Then
        verify(activityPersistence).findActive(PROCESS_ID);
        verify(behaviorResolver, times(2)).resolveCancellableStrategy(ActivityType.EXTERNAL_TASK);
        verify(cancellableBehaviour, times(2)).terminate(any(ActivityExecution.class));
        verify(taskExecutor, times(2)).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Should handle cancellable strategy not found gracefully")
    void shouldHandleCancellableStrategyNotFoundGracefully() {
        // Given
        var activity = createActivityExecution();
        var event = new TerminateActivityEvent(activity);

        when(behaviorResolver.resolveCancellableStrategy(ActivityType.EXTERNAL_TASK))
                .thenReturn(Optional.empty());

        // When
        activityCancelService.handleTerminate(event);

        // Then
        verify(behaviorResolver).resolveCancellableStrategy(ActivityType.EXTERNAL_TASK);
        verify(cancellableBehaviour, never()).terminate(any());
    }

    @Test
    @DisplayName("Should handle cancellation when no active activities found")
    void shouldHandleCancellationWhenNoActiveActivitiesFound() {
        // Given
        var emptyActivities = List.<ActivityExecution>of();
        var event = new CancelActivitiesByProcessIdEvent(PROCESS_ID);

        when(activityPersistence.findActive(PROCESS_ID)).thenReturn(emptyActivities);

        // When
        activityCancelService.handleCancel(event);

        // Then
        verify(activityPersistence).findActive(PROCESS_ID);
        verify(behaviorResolver, never()).resolveCancellableStrategy(any());
        verify(cancellableBehaviour, never()).cancel(any());
        verify(taskExecutor, never()).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Should handle termination when no active activities found")
    void shouldHandleTerminationWhenNoActiveActivitiesFound() {
        // Given
        var emptyActivities = List.<ActivityExecution>of();
        var event = new TerminateActivitiesByProcessIdEvent(PROCESS_ID);

        when(activityPersistence.findActive(PROCESS_ID)).thenReturn(emptyActivities);

        // When
        activityCancelService.handleTerminate(event);

        // Then
        verify(activityPersistence).findActive(PROCESS_ID);
        verify(behaviorResolver, never()).resolveCancellableStrategy(any());
        verify(cancellableBehaviour, never()).terminate(any());
        verify(taskExecutor, never()).submit(any(Runnable.class));
    }

    private ActivityExecution createActivityExecution() {
        var mockActivity = mock(ActivityExecution.class, withSettings().lenient());
        when(mockActivity.id()).thenReturn(ACTIVITY_ID);
        when(mockActivity.type()).thenReturn(ActivityType.EXTERNAL_TASK);
        when(mockActivity.state()).thenReturn(ActivityState.ACTIVE);
        when(mockActivity.toString()).thenReturn("ActivityExecution[" + ACTIVITY_ID + "]"); // Provide toString for event source
        return mockActivity;
    }

}