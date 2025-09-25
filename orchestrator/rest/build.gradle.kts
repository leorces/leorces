dependencies {
    // Project
    implementation(project(":orchestrator:api"))

    // Spring Boot
    implementation(libs.spring.boot.starter.web)

    // Swagger
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    // Development Tools
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    // Testing
    testImplementation(libs.bundles.testing)
}