package com.leorces.rest.client.configuration.properties.process;

import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

public record WorkerProperties(
        @DefaultValue Map<String, WorkerConfigProperties> workers
) {

}
