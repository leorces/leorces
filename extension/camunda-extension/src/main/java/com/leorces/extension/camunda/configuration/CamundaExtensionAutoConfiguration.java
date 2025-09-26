package com.leorces.extension.camunda.configuration;


import com.leorces.extension.camunda.CamundaExtensionService;
import com.leorces.extension.camunda.configuration.properties.CamundaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;


@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(CamundaProperties.class)
public class CamundaExtensionAutoConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    private final CamundaExtensionService camundaExtensionService;
    private final CamundaProperties properties;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        var bpmnPath = properties.bpmnPath();
        log.info("Processing BPMN files from configured path: {}", bpmnPath);

        try {
            camundaExtensionService.loadAndProcessBpmnFiles(bpmnPath);
            log.info("BPMN processing completed successfully");
        } catch (RuntimeException e) {
            log.error("Failed to process BPMN files", e);
            throw e;
        }
    }

}