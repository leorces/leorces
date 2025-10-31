# Leorces Engine Configuration Properties

This document describes the server-side configuration of the engine, available via Spring Boot properties/yaml. All
properties are set in application.yaml or application.properties.

Root prefix: `leorces`

All properties in one table:

| Property                                                    | Type                            | Default                                       | Description                                                                                                                                                                                      |
|-------------------------------------------------------------|---------------------------------|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `leorces.metrics.enabled`                                   | Boolean                         | `true`                                        | Enables publishing of engine metrics (Micrometer). Set to `false` to disable metrics.                                                                                                            |
| `leorces.compaction.enabled`                                | Boolean                         | `false`                                       | Registers the compaction scheduler when `true`. When `false`, the job is not scheduled.                                                                                                          |
| `leorces.compaction.batch-size`                             | Integer                         | `1000`                                        | Number of instances processed in a single run.                                                                                                                                                   |
| `leorces.compaction.cron`                                   | String                          | `0 0 0 * * *`                                 | Cron expression defining when the compaction job should run. The default runs daily at midnight.                                                                                                 |
| `leorces.processes.<processKey>.activity-retries`           | Integer                         | `0`                                           | Default number of retry attempts for activities within this process.                                                                                                                             |
| `leorces.processes.<processKey>.activity-timeout`           | String                          | `1h`                                          | Default activity timeout in a relative format. Supported units: days `d`, hours `h`, minutes `m`, seconds `s`, milliseconds `ms`. Combinations allowed, e.g., `1h 30m`, `2d 4h`, `45s`, `500ms`. |
| `leorces.processes.<processKey>.activities`                 | Map<String, ActivityProperties> | —                                             | Activity-level overrides keyed by External Task `topic`.                                                                                                                                         |
| `leorces.processes.<processKey>.activities.<topic>.retries` | Integer                         | Inherits process default (or `0` if not set)  | Override the number of retry attempts for this specific activity.                                                                                                                                |
| `leorces.processes.<processKey>.activities.<topic>.timeout` | String                          | Inherits process default (or `1h` if not set) | Override the timeout for this specific activity (same relative format as above).                                                                                                                 |

How compaction scheduling works:

- The scheduler is registered only when `leorces.compaction.enabled: true`.
- ShedLock is used for mutual exclusion with a 60-minute lock.
- On execution, the administrative operation `AdminService#doCompaction()` is invoked.

Resolution order for External Task settings (highest to lowest priority):

1) Value specified directly in the activity definition (BPMN), if present.
2) Activity-level override: `leorces.processes.<processKey>.activities.<topic>`.
3) Process-level default: `leorces.processes.<processKey>`.
4) Engine defaults: `retries = 0`, `timeout = 1h`.

Relative time format tips:

- Single interval: `10m`, `1h`, `2d`.
- Combinations: `1h 15m`, `1h 1m 1s 500ms`.

Examples:

```yaml
leorces:
  metrics:
    enabled: true
  compaction:
    enabled: true
    batch-size: 2000
    cron: 0 0 0 * * *
  processes:
    OrderSubmittedProcess:
      activities:
        notification:
          retries: 3
          timeout: 30m
    OrderFulfillmentProcess:
      activity-retries: 3
      activity-timeout: 10m
```
