dependencies {
    // Spring
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.logging)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Testing
    testImplementation(libs.bundles.testing)
}
