dependencies {
    // Spring Boot
    implementation(libs.spring.boot.starter.web)

    // Development Tools
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    // Testing
    testImplementation(libs.bundles.testing)
}