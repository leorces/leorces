package com.leorces.extension.camunda;


import com.leorces.api.DefinitionService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
@ComponentScan(basePackages = "com.leorces.extension.camunda")
public class BpmnParserTestConfiguration {

    @Bean
    @Primary
    public DefinitionService processDefinitionService() {
        return Mockito.mock(DefinitionService.class);
    }

}