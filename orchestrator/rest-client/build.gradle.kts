dependencies {
    // Project
    api(project(":orchestrator:api"))
    implementation(project(":orchestrator:common"))

    // Spring Boot
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.aop)

    // Micrometer & Prometheus
    implementation(libs.micrometer.core)
    implementation(libs.micrometer.registry.prometheus)

    // Jackson
    implementation(libs.bundles.jackson)

    // Resilience4j
    implementation(libs.bundles.resilience4j)

    // Development Tools
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)

    // Testing
    testImplementation(libs.bundles.testing)
}
