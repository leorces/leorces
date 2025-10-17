package com.leorces.rest.client.service;

import com.leorces.model.runtime.activity.ActivityFailure;
import com.leorces.rest.client.client.TaskRestClient;
import com.leorces.rest.client.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Task Service Implementation Tests")
class TaskServiceImplTest {

    private static final String TASK_ID = "test-task-123";
    private static final Map<String, Object> VARIABLES = Map.of("key1", "value1", "key2", 42);
    private static final Map<String, Object> EMPTY_VARIABLES = Collections.emptyMap();

    @Mock
    private TaskRestClient taskRestClient;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    @DisplayName("Should complete task without variables and return true on success")
    void shouldCompleteTaskWithoutVariablesAndReturnTrueOnSuccess() {
        //Given
        var task = createTask();
        var successResponse = new ResponseEntity<Void>(HttpStatus.OK);
        when(taskRestClient.complete(TASK_ID, EMPTY_VARIABLES)).thenReturn(successResponse);

        //When
        var result = taskService.complete(task);

        //Then
        verify(taskRestClient).complete(TASK_ID, EMPTY_VARIABLES);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should complete task with variables and return true on success")
    void shouldCompleteTaskWithVariablesAndReturnTrueOnSuccess() {
        //Given
        var task = createTask();
        var successResponse = new ResponseEntity<Void>(HttpStatus.OK);
        when(taskRestClient.complete(TASK_ID, VARIABLES)).thenReturn(successResponse);

        //When
        var result = taskService.complete(task, VARIABLES);

        //Then
        verify(taskRestClient).complete(TASK_ID, VARIABLES);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when task completion fails with non-2xx status")
    void shouldReturnFalseWhenTaskCompletionFailsWithNon2xxStatus() {
        //Given
        var task = createTask();
        var errorResponse = new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
        when(taskRestClient.complete(TASK_ID, EMPTY_VARIABLES)).thenReturn(errorResponse);

        //When
        var result = taskService.complete(task);

        //Then
        verify(taskRestClient).complete(TASK_ID, EMPTY_VARIABLES);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when task completion throws exception")
    void shouldReturnFalseWhenTaskCompletionThrowsException() {
        //Given
        var task = createTask();
        when(taskRestClient.complete(TASK_ID, EMPTY_VARIABLES))
                .thenThrow(new RuntimeException("Connection failed"));

        //When
        var result = taskService.complete(task);

        //Then
        verify(taskRestClient).complete(TASK_ID, EMPTY_VARIABLES);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should fail task without failure and variables and return true on success")
    void shouldFailTaskWithoutVariablesAndReturnTrueOnSuccess() {
        //Given
        var successResponse = new ResponseEntity<Void>(HttpStatus.OK);
        when(taskRestClient.fail(TASK_ID, null, EMPTY_VARIABLES)).thenReturn(successResponse);

        //When
        var result = taskService.fail(TASK_ID);

        //Then
        verify(taskRestClient).fail(TASK_ID, null, EMPTY_VARIABLES);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should fail task with failure and variables and return true on success")
    void shouldFailTaskWithVariablesAndReturnTrueOnSuccess() {
        //Given
        var failure = ActivityFailure.of("Failure reason");
        var successResponse = new ResponseEntity<Void>(HttpStatus.OK);
        when(taskRestClient.fail(TASK_ID, failure, VARIABLES)).thenReturn(successResponse);

        //When
        var result = taskService.fail(TASK_ID, failure, VARIABLES);

        //Then
        verify(taskRestClient).fail(TASK_ID, failure, VARIABLES);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when task failure operation fails with non-2xx status")
    void shouldReturnFalseWhenTaskFailureOperationFailsWithNon2xxStatus() {
        //Given
        var errorResponse = new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(taskRestClient.fail(TASK_ID, null, EMPTY_VARIABLES)).thenReturn(errorResponse);

        //When
        var result = taskService.fail(TASK_ID);

        //Then
        verify(taskRestClient).fail(TASK_ID, null, EMPTY_VARIABLES);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when task failure operation throws exception")
    void shouldReturnFalseWhenTaskFailureOperationThrowsException() {
        //Given
        when(taskRestClient.fail(TASK_ID, null, EMPTY_VARIABLES))
                .thenThrow(new RuntimeException("Network error"));

        //When
        var result = taskService.fail(TASK_ID);

        //Then
        verify(taskRestClient).fail(TASK_ID, null, EMPTY_VARIABLES);
        assertThat(result).isFalse();
    }

    private Task createTask() {
        return Task.builder()
                .id(TaskServiceImplTest.TASK_ID)
                .build();
    }

}