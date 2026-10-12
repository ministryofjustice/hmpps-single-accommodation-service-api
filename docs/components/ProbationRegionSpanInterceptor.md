# ProbationRegionSpanInterceptor

Adds the caller's probation region to the current OpenTelemetry span, so traces in Application Insights can be filtered and grouped by region.

- Interceptor: `infrastructure/.../infrastructure/tracing/ProbationRegionSpanInterceptor.kt`
- Test: `src/test/.../integration/tracing/ProbationRegionSpanInterceptorIT.kt`
- Test support: `src/test/.../config/TestOpenTelemetryConfig.kt`

## How it works

`ProbationRegionSpanInterceptor` is a Spring MVC `HandlerInterceptor`, registered for all requests by `ProbationRegionSpanInterceptorConfig` (a `WebMvcConfigurer`).

In `preHandle`:

1. Read the `x-user-probation-region` request header.
2. If it is absent or blank, do nothing.
3. Otherwise take `Span.current()`. If its span context is valid, set the attribute `user.probation.region` to the header value.
4. Always return `true`, so the request continues.

The interceptor never creates a span. It only enriches the server span the OpenTelemetry Java agent already creates for the request. If there is no active span (tracing disabled, local runs, most unit tests), `Span.current()` is a no-op span with an invalid context and the interceptor does nothing.

## Why an interceptor rather than a `OncePerRequestFilter`

Both would work technically. The interceptor was chosen because:

- **Scope:** it runs only for requests dispatched to a Spring MVC handler. A filter also runs for static resources, actuator endpoints, error dispatches and anything else in the servlet chain, where a region attribute is not useful.
- **Simplicity:** `OncePerRequestFilter` needs filter ordering decisions (it must run after the agent's span is started and, if later reading the authenticated user, after security). The interceptor runs inside the MVC dispatch, after the agent's servlet instrumentation and security filters, so ordering is not a concern.
- **Consistency:** the repo already uses this pattern in `SentryRequestContextInterceptor`.
- **Failure handling:** `preHandle` runs after the span has started and cannot affect the filter chain. Nothing needs wrapping in try/finally.

Trade-off: requests rejected before reaching MVC (e.g. a 401 from the security filter) will not carry the attribute. If that is needed, a filter would be required.

## What it looks like in telemetry

The attribute appears on the server request span as a custom dimension:

| Field | Value |
|---|---|
| Span | the incoming HTTP server span (e.g. `GET /user/teams`) |
| Attribute | `user.probation.region` |
| Value | the header value, e.g. `London` |

In Application Insights it appears in `requests` under `customDimensions["user.probation.region"]`. Example query:

```kusto
requests
| where isnotempty(customDimensions["user.probation.region"])
| summarize count(), avg(duration) by tostring(customDimensions["user.probation.region"])
```

Requests without the header have no such attribute. Child spans (dependencies) do not inherit it, because attributes are set only on the server span.

## How the tests work

The test environment has no OpenTelemetry agent, so `Span.current()` would normally be invalid and there would be nothing to assert on.

`TestOpenTelemetryConfig` (a `@TestConfiguration`) provides:

- An `InMemorySpanExporter` bean that records finished spans.
- A filter, registered with highest precedence, that starts a real SDK span (`test-request`, kind `SERVER`) before the request, makes it current for the request thread, and ends it afterwards. This stands in for the agent.

`ProbationRegionSpanInterceptorIT` imports that config and calls `/user/teams` as a Delius user through the full application:

- **Header sent:** with `x-user-probation-region` set, the single recorded span has `user.probation.region` equal to that value.
- **Header absent:** the single recorded span has no `user.probation.region` attribute.

Assertions use Awaitility because the span is ended and exported after the response is returned. The exporter is reset before each test.

Run it from the root project (the `infrastructure` module has no integration tests of its own):

```
./gradlew :integrationTest --tests '*ProbationRegionSpanInterceptorIT'
```

