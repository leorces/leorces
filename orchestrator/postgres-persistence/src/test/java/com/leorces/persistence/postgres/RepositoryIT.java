package com.leorces.persistence.postgres;

import com.leorces.model.runtime.process.Process;
import com.leorces.persistence.*;
import com.leorces.persistence.postgres.cache.DefinitionCache;
import com.leorces.persistence.postgres.repository.*;
import com.leorces.persistence.postgres.utils.ProcessDefinitionTestData;
import com.leorces.persistence.postgres.utils.ProcessTestData;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class RepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16.1");

    static {
        database.withReuse(true);
        database.start();
    }

    @Autowired
    protected DefinitionPersistence definitionPersistence;
    @Autowired
    protected ProcessPersistence processPersistence;
    @Autowired
    protected ActivityPersistence activityPersistence;
    @Autowired
    protected VariablePersistence variablePersistence;
    @Autowired
    protected HistoryPersistence historyPersistence;
    @Autowired
    protected JobPersistence jobPersistence;
    @Autowired
    protected AdminPersistence adminPersistence;
    @Autowired
    protected DefinitionRepository definitionRepository;
    @Autowired
    protected ProcessRepository processRepository;
    @Autowired
    protected ActivityRepository activityRepository;
    @Autowired
    protected VariableRepository variableRepository;
    @Autowired
    protected ShedlockRepository shedlockRepository;
    @Autowired
    protected HistoryRepository historyRepository;
    @Autowired
    protected JobRepository jobRepository;
    @Autowired
    protected DefinitionCache definitionCache;

    @DynamicPropertySource
    private static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> String.format("jdbc:postgresql://%s:%d/%s", database.getHost(), database.getFirstMappedPort(), database.getDatabaseName()));
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);

        registry.add("spring.liquibase.enabled", () -> true);
    }

    protected Process createOrderSubmittedProcess() {
        var processDefinition = definitionPersistence.save(List.of(ProcessDefinitionTestData.createOrderSubmittedProcessDefinition())).getFirst();
        return ProcessTestData.createOrderSubmittedProcess(processDefinition);
    }

    protected Process runOrderSubmittedProcess() {
        var processDefinition = definitionPersistence.save(List.of(ProcessDefinitionTestData.createOrderSubmittedProcessDefinition())).getFirst();
        var process = ProcessTestData.createOrderSubmittedProcess(processDefinition);
        return processPersistence.run(process);
    }

    protected Process runOrderFulfilledProcess() {
        var processDefinition = definitionPersistence.save(List.of(ProcessDefinitionTestData.createOrderFulfillmentProcessDefinition())).getFirst();
        var process = ProcessTestData.createOrderFulfillmentProcess(processDefinition);
        return processPersistence.run(process);
    }

    @AfterEach
    void afterEach() {
        definitionRepository.deleteAll();
        processRepository.deleteAll();
        activityRepository.deleteAll();
        variableRepository.deleteAll();
        shedlockRepository.deleteAll();
        historyRepository.deleteAll();
        jobRepository.deleteAll();
        definitionCache.invalidateAll();
    }

}

