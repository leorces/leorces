package com.leorces.rest.client.configuration.properties.resilence;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "leorces.resilence")
public record ResilenceConfigurationProperties(
        ResilenceProperties taskPoll,
        ResilenceProperties taskUpdate
) {

}
