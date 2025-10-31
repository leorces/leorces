# Leorces REST Client Configuration Properties

This document describes the client-side configuration available via Spring Boot properties/yaml for the REST client and
workers. All properties are set in application.yaml or application.properties.

Root prefix: `leorces`

All properties in one table:

| Property                                                                                     | Type     | Default                 | Description                                                                           |
|----------------------------------------------------------------------------------------------|----------|-------------------------|---------------------------------------------------------------------------------------|
| `leorces.rest.host`                                                                          | String   | `http://localhost:8080` | Base URL of the orchestrator REST API.                                                |
| `leorces.rest.connect-timeout`                                                               | Duration | `PT30S`                 | Connection timeout used by the HTTP client. Examples: `30s`, `1m`.                    |
| `leorces.rest.read-timeout`                                                                  | Duration | `PT60S`                 | Read timeout used by the HTTP client.                                                 |
| `leorces.rest.write-timeout`                                                                 | Duration | `PT30S`                 | Write timeout used by the HTTP client.                                                |
| `leorces.rest.max-connections`                                                               | Integer  | `100`                   | Maximum total HTTP connections in the pool.                                           |
| `leorces.rest.max-connections-per-route`                                                     | Integer  | `20`                    | Maximum HTTP connections per route (per host).                                        |
| `leorces.rest.keep-alive-timeout`                                                            | Duration | `PT2M`                  | Keep-alive timeout for persistent HTTP connections.                                   |
| `leorces.rest.enable-retry-on-connection-failure`                                            | Boolean  | `true`                  | Enables low-level retry on connection failures in the HTTP client.                    |
| `leorces.resilence.task-poll.circuit-breaker.failure-rate-threshold`                         | Float    | `50.0`                  | Failure rate threshold (%) that opens the circuit for task polling.                   |
| `leorces.resilence.task-poll.circuit-breaker.wait-duration-in-open-state`                    | Duration | `30s`                   | How long the circuit stays open before trying half-open for task polling.             |
| `leorces.resilence.task-poll.circuit-breaker.sliding-window-size`                            | Integer  | `10`                    | Number of calls recorded for calculating failure rate (task polling).                 |
| `leorces.resilence.task-poll.circuit-breaker.minimum-number-of-calls`                        | Integer  | `5`                     | Minimum number of calls before failure rate is calculated (task polling).             |
| `leorces.resilence.task-poll.circuit-breaker.permitted-number-of-calls-in-half-open-state`   | Integer  | `3`                     | Calls permitted in half-open state before switching (task polling).                   |
| `leorces.resilence.task-update.circuit-breaker.failure-rate-threshold`                       | Float    | `50.0`                  | Failure rate threshold (%) that opens the circuit for task updates.                   |
| `leorces.resilence.task-update.circuit-breaker.wait-duration-in-open-state`                  | Duration | `30s`                   | How long the circuit stays open before trying half-open for task updates.             |
| `leorces.resilence.task-update.circuit-breaker.sliding-window-size`                          | Integer  | `10`                    | Number of calls recorded for calculating failure rate (task updates).                 |
| `leorces.resilence.task-update.circuit-breaker.minimum-number-of-calls`                      | Integer  | `5`                     | Minimum number of calls before failure rate is calculated (task updates).             |
| `leorces.resilence.task-update.circuit-breaker.permitted-number-of-calls-in-half-open-state` | Integer  | `3`                     | Calls permitted in half-open state before switching (task updates).                   |
| `leorces.resilence.task-update.retry.max-attempts`                                           | Integer  | `3`                     | Maximum retry attempts for task update requests.                                      |
| `leorces.resilence.task-update.retry.wait-duration`                                          | Duration | `1s`                    | Wait duration between retry attempts for task updates.                                |
| `leorces.process.<processKey>.workers.<workerName>.interval`                                 | Long     | —                       | Worker execution interval amount. Must be used together with `time-unit`.             |
| `leorces.process.<processKey>.workers.<workerName>.time-unit`                                | TimeUnit | —                       | Interval time unit. Allowed values: `milliseconds`, `seconds`, `minutes`, etc.        |
| `leorces.process.<processKey>.workers.<workerName>.initial-delay`                            | Long     | —                       | Initial delay before the first worker run, in the specified `time-unit`.              |
| `leorces.process.<processKey>.workers.<workerName>.max-concurrent-tasks`                     | Integer  | —                       | Maximum number of concurrent tasks that this worker can process.                      |
| `leorces.metrics.enabled`                                                                    | Boolean  | `true`                  | Enables publishing of client metrics (Micrometer). Set to `false` to disable metrics. |

Notes and tips:

- Resilience mapping: two separately configured operations are used under the hood:
    - `task-poll` — polling available external tasks from the orchestrator.
    - `task-update` — updating/completing tasks back to the orchestrator.
      Circuit breaker instances are registered under these names. Retries are applied only to `task-update` by design (
      polling does not retry to avoid bursty traffic).
- Durations: Spring Boot supports ISO-8601 (e.g., `PT30S`) and convenient suffix format (e.g., `30s`, `2m`, `1h`,
  `500ms`).
- Worker configuration: `<processKey>` equals your BPMN processDefinitionKey; `<workerName>` typically matches the
  external task topic. The `interval` and `time-unit` define how often the worker runs; `initial-delay` postpones the
  very first run; `max-concurrent-tasks` controls concurrency per worker.

Example:

```yaml
leorces:
  rest:
    host: http://localhost:8080
    connect-timeout: 30s
    read-timeout: 60s
    write-timeout: 30s
    max-connections: 100
    max-connections-per-route: 20
    keep-alive-timeout: 2m
    enable-retry-on-connection-failure: true

  resilence:
    task-poll:
      circuit-breaker:
        failure-rate-threshold: 60.0
        wait-duration-in-open-state: 15s
        sliding-window-size: 20
        minimum-number-of-calls: 10
        permitted-number-of-calls-in-half-open-state: 5
    task-update:
      circuit-breaker:
        failure-rate-threshold: 70.0
        wait-duration-in-open-state: 10s
        sliding-window-size: 15
        minimum-number-of-calls: 8
        permitted-number-of-calls-in-half-open-state: 3
      retry:
        max-attempts: 2
        wait-duration: 500ms

  process:
    configuration:
      OrderSubmittedProcess:
        workers:
          notification:
            interval: 5
            time-unit: seconds
            max-concurrent-tasks: 100
      OrderPaymentProcess:
        workers:
          debit:
            interval: 500
            time-unit: milliseconds
            initial-delay: 250
            max-concurrent-tasks: 100
          deposit:
            interval: 500
            time-unit: milliseconds
            initial-delay: 500
            max-concurrent-tasks: 100

  metrics:
    enabled: true
```