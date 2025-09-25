package com.leorces.rest.client.service;

import com.leorces.api.RepositoryService;
import com.leorces.rest.client.client.RepositoryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private final RepositoryClient repositoryClient;

    @Override
    public void doCompaction() {
        repositoryClient.doCompaction();
    }
}
