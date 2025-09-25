package com.leorces.rest.client.configuration.properties.process;

import java.util.Map;

public record WorkerProperties(
        Map<String, WorkerConfigProperties> workers
) {

}
