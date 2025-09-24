rootProject.name = "leorces"

// Orchestrator
include("orchestrator")
include("orchestrator:model")
include("orchestrator:api")
include("orchestrator:engine")
include("orchestrator:rest")
include("orchestrator:rest-client")
include("orchestrator:persistence-api")
include("orchestrator:postgres-persistence")
include("orchestrator:juel")
include("orchestrator:common")
include("orchestrator:ui")

// Extensions
include("extension")
include("extension:camunda-extension")

// Examples
include("example")
include("example:client-example")
include("example:server-with-camunda-extension-example")