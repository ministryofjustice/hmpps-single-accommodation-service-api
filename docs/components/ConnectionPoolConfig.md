# Connection Pool Configuration

This document explains why `ConnectionPoolConfig` exists, what problem it solves, and how it is configured.

## Overview

The service builds outbound HTTP clients through `hmpps-kotlin-lib`'s `authorisedWebClient(...)` helper inside `HttpServiceProxiesConfig`. That helper creates Reactor Netty `HttpClient` instances via `HttpClient.create()`.

`HttpClient.create()` uses Reactor Netty's JVM-wide default resources (`HttpResources`), including the default connection pool. If we leave that pool on its defaults, stale or long-idle connections can be reused after an upstream load balancer, NAT, or peer has silently closed them.

That can surface as intermittent outbound failures such as `Connection reset by peer`.

## Why the current approach is the right one

The code uses `HttpResources.set(...)` to replace Reactor Netty's default connection pool early in Spring startup.

This is the right approach here because:

1. our outbound clients are not constructed with a custom `ConnectionProvider`
2. `authorisedWebClient(...)` hides the underlying `HttpClient.create()` call
3. the OAuth2 token client inside that helper also relies on the same Reactor Netty defaults
4. swapping the JVM-wide default pool fixes both the main API clients and the token-fetching client without re-implementing library code

## What the configuration does

`ConnectionPoolConfig` creates a `ConnectionProvider` and installs it as the Reactor Netty default.

The configured settings are:

| Property | Default | What it does |
|---|---:|---|
| `service.connection-pool.max-idle-time` | `30s` | Evicts connections that have sat idle too long before they can be reused |
| `service.connection-pool.max-life-time` | `5m` | Recycles long-lived connections even if they are otherwise healthy |
| `service.connection-pool.eviction-interval` | `30s` | Runs background cleanup so stale connections are removed even during quiet periods |
| `service.connection-pool.pool-name` | `default-http-pool` | Names the Reactor Netty pool for identification and diagnostics |

The configuration is supplied through `ConnectionPoolProperties`, which binds the `service.connection-pool` section from `application-infrastructure.yml`.

`ConnectionPoolConfig` focuses on wiring:

- read the typed `ConnectionPoolProperties`
- build the `ConnectionProvider`
- install it into `HttpResources`

This keeps the configuration easy to understand and allows the timing values to be tuned through configuration in the same way as the service's other outbound HTTP settings.

## Why not use a per-client connection provider?

That would normally be a reasonable design, but it is not the best fit here because the relevant `WebClient` creation is delegated to `hmpps-kotlin-lib`. Rebuilding that stack just to inject a pool would add duplication and risk diverging from the shared library's OAuth2 and proxy handling.

## Configuration considerations

The connection pool timings are configurable so that environments can tune them without recompiling the service. This is useful when different network paths, load balancers, or upstream behaviour require different eviction settings.

That flexibility also increases the configuration surface area. When troubleshooting outbound HTTP behaviour, remember that connection reuse and eviction can be affected by the configured values for:

- `service.connection-pool.max-idle-time`
- `service.connection-pool.max-life-time`
- `service.connection-pool.eviction-interval`

The pool name is also configurable through `service.connection-pool.pool-name`. In practice this is mainly useful for diagnostics and identification rather than behaviour.

## Trade-offs and caveats

- `HttpResources.set(...)` changes the Reactor Netty default at JVM scope, so this configuration is intentionally global.
- Any other component in the same application that relies on Reactor Netty's default resources can pick up this connection pool configuration as well.
- Tests that replace Reactor Netty defaults should restore the original state so they do not leak configuration across test cases.
- The unit test still uses reflection to verify Reactor Netty internal builder settings. That is slightly brittle, but it gives confidence that the provider really carries the configured timings.
- If future environments need different tuning, they can override `service.connection-pool.*` through configuration.

## Validation considerations

Because the pool settings are externally configurable, invalid duration values can affect startup or runtime behaviour. If this configuration becomes more widely tuned between environments, it may be worth adding explicit validation to `ConnectionPoolProperties` so invalid values fail fast during application startup.

## Related source files

- `infrastructure/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/infrastructure/config/ConnectionPoolConfig.kt`
- `infrastructure/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/infrastructure/config/HttpServiceProxiesConfig.kt`
- `infrastructure/src/main/resources/application-infrastructure.yml`

