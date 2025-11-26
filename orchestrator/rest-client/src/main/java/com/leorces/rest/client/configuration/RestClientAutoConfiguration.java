package com.leorces.rest.client.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.leorces.common.service.MetricService;
import com.leorces.common.service.impl.MetricServiceImpl;
import com.leorces.common.service.impl.MicrometerMetricService;
import com.leorces.rest.client.configuration.properties.metrics.MetricsProperties;
import com.leorces.rest.client.configuration.properties.process.ProcessConfigurationProperties;
import com.leorces.rest.client.configuration.properties.resilence.ResilenceConfigurationProperties;
import com.leorces.rest.client.configuration.properties.rest.RestClientProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({
        RestClientProperties.class,
        ProcessConfigurationProperties.class,
        ResilenceConfigurationProperties.class,
        MetricsProperties.class
})
public class RestClientAutoConfiguration {

    @Bean("leorcesRestClientObjectMapper")
    public ObjectMapper leorcesRestClientObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean("leorcesRestClient")
    public RestClient leorcesRestClient(RestClientProperties properties,
                                        @Qualifier("leorcesRestClientObjectMapper") ObjectMapper leorcesRestClientObjectMapper) {
        var messageConverter = new MappingJackson2HttpMessageConverter(leorcesRestClientObjectMapper);
        var requestFactory = createRequestFactory(properties);

        return RestClient.builder()
                .baseUrl(properties.host())
                .requestFactory(requestFactory)
                .messageConverters(converters -> {
                    converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
                    converters.add(messageConverter);
                })
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "leorces.metrics.enabled", havingValue = "true", matchIfMissing = true)
    public MetricService metricService(MeterRegistry meterRegistry,
                                       MetricsProperties metricsProperties) {
        var micrometerService = new MicrometerMetricService(meterRegistry);
        return new MetricServiceImpl(micrometerService, metricsProperties.enabled());
    }

    private SimpleClientHttpRequestFactory createRequestFactory(RestClientProperties properties) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        factory.setReadTimeout((int) properties.readTimeout().toMillis());
        return factory;
    }

}
