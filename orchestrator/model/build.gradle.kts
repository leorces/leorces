dependencies {
    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Jackson
    implementation(libs.bundles.jackson)

    // Testing
    testImplementation(libs.bundles.testing)
}