plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    // Project
    implementation(project(":orchestrator:rest-client"))

    // Spring Boot
    implementation(libs.spring.boot.starter.web)

    // Development Tools
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
}