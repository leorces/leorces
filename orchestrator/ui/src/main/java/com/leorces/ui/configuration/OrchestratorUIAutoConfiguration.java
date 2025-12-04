package com.leorces.ui.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceTransformer;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@AutoConfiguration
@ComponentScan(basePackages = "com.leorces.ui")
public class OrchestratorUIAutoConfiguration implements WebMvcConfigurer {

    @Value("${app.base-path:/}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addTransformer(new BaseHrefResourceTransformer(basePath))
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        return requestedResource.exists() && requestedResource.isReadable()
                                ? requestedResource
                                : location.createRelative("index.html");
                    }
                });
    }

    private record BaseHrefResourceTransformer(String basePath) implements ResourceTransformer {

        private BaseHrefResourceTransformer(String basePath) {
            this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
        }

        @Override
        public Resource transform(HttpServletRequest request, Resource resource,
                                  ResourceTransformerChain chain) throws IOException {
            var transformed = chain.transform(request, resource);
            var content = new String(transformed.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            content = content.replace("${app.base-path}", basePath);
            return new TransformedResource(transformed, content.getBytes(StandardCharsets.UTF_8));
        }

    }

}
