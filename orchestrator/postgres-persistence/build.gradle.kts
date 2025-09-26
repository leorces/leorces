dependencies {
    // Project
    implementation(project(":orchestrator:model"))
    implementation(project(":orchestrator:persistence-api"))

    // Spring Boot
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.autoconfigure)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Database
    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.postgresql)
    implementation(libs.liquibase.core)

    // JSON Processing
    implementation(libs.bundles.jackson)

    // Development Tools
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)

    // Testing
    testImplementation(libs.bundles.testing.full)
}
