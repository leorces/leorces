package com.leorces.rest.client.configuration.properties.resilence;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "leorces.resilence")
public record ResilenceConfigurationProperties(
        @DefaultValue ResilenceProperties taskPoll,
        @DefaultValue ResilenceProperties taskUpdate
) {

}
