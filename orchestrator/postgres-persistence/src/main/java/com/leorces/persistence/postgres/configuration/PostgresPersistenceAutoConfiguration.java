package com.leorces.persistence.postgres.configuration;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.leorces.model.definition.activity.ActivityDefinition;
import com.leorces.model.definition.activity.ActivityDefinitionDeserializer;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import javax.sql.DataSource;

@AutoConfiguration
@EnableConfigurationProperties(LiquibaseProperties.class)
@EnableJdbcRepositories(basePackages = "com.leorces.persistence.postgres.repository")
public class PostgresPersistenceAutoConfiguration {

    private static final String LIQUIBASE_CHANGELOG_PATH = "classpath:db/changelog/db.changelog-master.xml";

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.findAndRegisterModules();
        var module = new SimpleModule();
        module.addDeserializer(ActivityDefinition.class, new ActivityDefinitionDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringLiquibase liquibase(DataSource dataSource,
                                     LiquibaseProperties liquibaseProperties) {
        var liquibase = createSpringLiquibase(dataSource);
        configureLiquibase(liquibase, liquibaseProperties);
        return liquibase;
    }

    private SpringLiquibase createSpringLiquibase(DataSource dataSource) {
        var liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(LIQUIBASE_CHANGELOG_PATH);
        return liquibase;
    }

    private void configureLiquibase(SpringLiquibase liquibase, LiquibaseProperties properties) {
        setContextsIfPresent(liquibase, properties);
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(true);
    }

    private void setContextsIfPresent(SpringLiquibase liquibase, LiquibaseProperties properties) {
        if (properties.getContexts() != null) {
            liquibase.setContexts(String.join(",", properties.getContexts()));
        }
    }

}
