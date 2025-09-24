dependencies {
    // Project
    implementation(project(":orchestrator:api"))

    // Spring Boot
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.autoconfigure)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Development Tools
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    // Testing
    testImplementation(libs.bundles.testing)
}