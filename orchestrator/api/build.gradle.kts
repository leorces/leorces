dependencies {
    // Project
    api(project(":orchestrator:model"))

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}