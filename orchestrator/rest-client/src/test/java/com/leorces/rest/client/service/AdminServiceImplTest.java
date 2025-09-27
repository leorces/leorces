package com.leorces.rest.client.service;

import com.leorces.rest.client.client.RepositoryClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Repository Service Implementation Tests")
class AdminServiceImplTest {

    @Mock
    private RepositoryClient repositoryClient;

    @InjectMocks
    private AdminServiceImpl repositoryService;

    @Test
    @DisplayName("Should perform compaction by delegating to repository client")
    void shouldPerformCompactionByDelegatingToRepositoryClient() {
        //When
        repositoryService.doCompaction();

        //Then
        verify(repositoryClient).doCompaction();
    }

}