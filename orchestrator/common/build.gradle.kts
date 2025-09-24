dependencies {
    // Project
    implementation(project(":orchestrator:model"))

    // Spring
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.actuator)

    // Micrometer & Prometheus
    implementation(libs.micrometer.core)
    implementation(libs.micrometer.registry.prometheus)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Jackson
    implementation(libs.bundles.jackson)

    // Testing
    testImplementation(libs.bundles.testing)
}