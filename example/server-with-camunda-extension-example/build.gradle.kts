plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    // Project
    implementation(project(":orchestrator:model"))
    implementation(project(":orchestrator:engine"))
    implementation(project(":orchestrator:rest"))
    implementation(project(":orchestrator:ui"))
    implementation(project(":orchestrator:postgres-persistence"))
    implementation(project(":extension:camunda-extension"))

    // Spring
    implementation(libs.spring.boot.starter.web)
}