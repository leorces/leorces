dependencies {
    // Project
    api(project(":orchestrator:api"))
    implementation(project(":orchestrator:common"))
    implementation(project(":orchestrator:juel"))
    implementation(project(":orchestrator:persistence-api"))

    // Spring
    implementation(libs.spring.boot.starter)

    // Micrometer & Prometheus
    implementation(libs.micrometer.core)
    implementation(libs.micrometer.registry.prometheus)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Utils
    implementation(libs.commons.lang3)

    // Jackson
    implementation(libs.bundles.jackson)

    // Testing
    testImplementation(libs.bundles.testing)
}