package com.leorces.engine;

import com.leorces.engine.activity.command.*;
import com.leorces.engine.core.CommandDispatcher;
import com.leorces.model.runtime.activity.Activity;
import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.persistence.ActivityPersistence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityServiceImpl Tests")
class ActivityServiceImplTest {

    @Mock
    private ActivityPersistence activityPersistence;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private ActivityServiceImpl service;

    @Test
    @DisplayName("run should dispatch RunActivityCommand")
    void runDispatchesCommand() {
        var definitionId = "def-1";
        var processId = "proc-1";

        service.run(definitionId, processId);

        verify(dispatcher).dispatchAsync(argThat(cmd ->
                cmd instanceof RunActivityCommand &&
                        ((RunActivityCommand) cmd).definitionId().equals(definitionId) &&
                        ((RunActivityCommand) cmd).processId().equals(processId)
        ));
        verifyNoMoreInteractions(dispatcher, activityPersistence);
    }

    @Test
    @DisplayName("complete should dispatch CompleteActivityCommand")
    void completeDispatchesCommand() {
        var activityId = "act-1";
        Map<String, Object> vars = Map.of("key", "value");

        service.complete(activityId, vars);

        verify(dispatcher).dispatchAsync(argThat(cmd ->
                cmd instanceof CompleteActivityCommand &&
                        ((CompleteActivityCommand) cmd).activityId().equals(activityId)
        ));
        verifyNoMoreInteractions(dispatcher, activityPersistence);
    }

    @Test
    @DisplayName("fail should dispatch FailActivityCommand")
    void failDispatchesCommand() {
        var activityId = "act-2";
        Map<String, Object> vars = Map.of("foo", "bar");

        service.fail(activityId, vars);

        verify(dispatcher).dispatchAsync(argThat(cmd ->
                cmd instanceof FailActivityCommand &&
                        ((FailActivityCommand) cmd).activityId().equals(activityId)
        ));
        verifyNoMoreInteractions(dispatcher, activityPersistence);
    }

    @Test
    @DisplayName("fail(activityId, failure) should dispatch FailActivityCommand with given failure and empty variables")
    void failWithFailureDispatchesCommand() {
        var activityId = "act-5";
        var failure = new ActivityFailure("test-reason", "stacktrace");

        service.fail(activityId, failure);

        verify(dispatcher).dispatchAsync(argThat(cmd ->
                cmd instanceof FailActivityCommand &&
                        ((FailActivityCommand) cmd).activityId().equals(activityId) &&
                        ((FailActivityCommand) cmd).failure().equals(failure) &&
                        ((FailActivityCommand) cmd).variables().isEmpty()
        ));
        verifyNoMoreInteractions(dispatcher, activityPersistence);
    }

    @Test
    @DisplayName("fail(activityId, failure, variables) should dispatch FailActivityCommand with given failure and variables")
    void failWithFailureAndVariablesDispatchesCommand() {
        var activityId = "act-6";
        var failure = new ActivityFailure("bad-input", "trace-xyz");
        Map<String, Object> vars = Map.of("error", "invalid-state");

        service.fail(activityId, failure, vars);

        verify(dispatcher).dispatchAsync(argThat(cmd ->
                cmd instanceof FailActivityCommand &&
                        ((FailActivityCommand) cmd).activityId().equals(activityId) &&
                        ((FailActivityCommand) cmd).failure().equals(failure) &&
                        ((FailActivityCommand) cmd).variables().equals(vars)
        ));
        verifyNoMoreInteractions(dispatcher, activityPersistence);
    }

    @Test
    @DisplayName("terminate should dispatch TerminateActivityCommand")
    void terminateDispatchesCommand() {
        var activityId = "act-3";

        service.terminate(activityId);

        verify(dispatcher).dispatchAsync(argThat(cmd ->
                cmd instanceof TerminateActivityCommand &&
                        ((TerminateActivityCommand) cmd).activityId().equals(activityId)
        ));
        verifyNoMoreInteractions(dispatcher, activityPersistence);
    }

    @Test
    @DisplayName("retry should dispatch RetryActivityCommand")
    void retryDispatchesCommand() {
        var activityId = "act-4";

        service.retry(activityId);

        verify(dispatcher).dispatchAsync(argThat(cmd ->
                cmd instanceof RetryActivityCommand &&
                        ((RetryActivityCommand) cmd).activityId().equals(activityId)
        ));
        verifyNoMoreInteractions(dispatcher, activityPersistence);
    }

    @Test
    @DisplayName("poll should delegate to activityPersistence")
    void pollDelegates() {
        var topic = "topic1";
        var processKey = "procKey1";
        var limit = 5;
        List<Activity> activities = List.of(mock(Activity.class));

        when(activityPersistence.poll(topic, processKey, limit)).thenReturn(activities);

        var result = service.poll(topic, processKey, limit);

        assertThat(result).isEqualTo(activities);
        verify(activityPersistence).poll(topic, processKey, limit);
        verifyNoMoreInteractions(activityPersistence);
        verifyNoInteractions(dispatcher);
    }

}
