dependencies {
    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Jackson
    implementation(libs.bundles.jackson)

    // Utils
    implementation(libs.commons.lang3)

    // Testing
    testImplementation(libs.bundles.testing)
}