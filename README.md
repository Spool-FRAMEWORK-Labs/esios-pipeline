# esios-pipeline

> A data ingestion pipeline for the **Spanish Electricity Grid (REE) ESIOS API** built on top of [Spool](https://github.com/spool-framework), a reactive data pipeline framework for Java.

---

## What does this project do?

This example demonstrates how to use Spool to build a complete pipeline that:

1. **Polls every 10 minutes** the REE ESIOS API for energy market indicator data (indicator `1293`)
2. **Normalizes** the JSON array of indicator values into individual events
3. **Enriches** each event with the parent indicator ID from the API response
4. **Persists** events to a local data lake via the ingester module
5. **Cleans up** expired events automatically through the janitor module

---

## Pipeline architecture

The entire pipeline is declared in a single YAML file (`pipeline.yaml`):

```yaml
modules:
  - crawler:        # Polls the ESIOS API on a schedule
      type: POLL
      source:
        type: ESIOS_HTTP  # Custom plugin: EsiosHTTPPollSource

  - ingester:       # Persists events to the data lake
      type: REACTIVE

  - janitor:        # Removes events that exceed the configured TTL
```

```
┌──────────────────────────────────────────────────────────┐
│                    Spool Runtime                         │
│                                                          │
│  ┌──────────┐    ┌───────────┐    ┌───────────────┐     │
│  │ Crawler  │───▶│ Event Bus │───▶│   Ingester    │     │
│  │  (POLL)  │    │(IN_MEMORY)│    │  (REACTIVE)   │     │
│  └──────────┘    └───────────┘    └───────┬───────┘     │
│       │                                   │             │
│  REE ESIOS API                     Data Lake (FS)       │
│  (HTTP + API key)                                        │
│                          ┌────────────┐                 │
│                          │   Janitor  │                 │
│                          │   (TTL)    │                 │
│                          └────────────┘                 │
└──────────────────────────────────────────────────────────┘
```

---

## Custom plugin

Spool uses a Java SPI-based plugin system. This example implements one plugin:

### `EsiosHTTPPollSource` / `EsiosHTTPPollSourceProvider`

A POLL source that authenticates against the REE ESIOS API using an `x-api-key` header and fetches indicator values on a configurable schedule.

```java
@SpoolPlugin(PollSourceProvider.class)
public class EsiosHTTPPollSourceProvider implements PollSourceProvider {
    @Override public String name() { return "ESIOS_HTTP"; }
    // ...
}
```

### Enrichment

The pipeline enriches each value event with `indicator_id` extracted from the parent `indicator.id` field in the response, keeping the indicator context on every individual record:

```yaml
enrichmentList:
  - source: indicator.id
    target: indicator_id
```

---

## Pipeline configuration

| Parameter | Value |
|-----------|-------|
| API URL | `https://api.esios.ree.es/indicators/1293` |
| Auth | `x-api-key` header |
| Poll interval | 600 000 ms (10 min) |
| Root path | `indicator.values` |
| Naming convention | `SNAKE_CASE` |
| Janitor TTL | 600 000 ms |

---

## Project structure

```
esios-pipeline/
├── src/main/java/software/examples/spool/esios/
│   ├── Application.java                        # Runtime configuration
│   ├── Main.java                               # Entry point
│   └── plugins/
│       ├── EsiosHTTPPollSource.java            # HTTP client for the ESIOS API
│       └── EsiosHTTPPollSourceProvider.java    # SPI plugin: poll source
├── src/main/resources/
│   └── pipeline.yaml                          # Pipeline declaration
└── pom.xml
```

---

## Requirements

| Tool  | Minimum version |
|-------|----------------|
| Java  | 21             |
| Maven | 3.8+           |
| Spool | 1.1.2          |

> **API key required** — you need a valid REE ESIOS API key. Set it in `pipeline.yaml` under `source.configuration.apiKey`.

---

## Running the pipeline

### 1. Build

```bash
mvn package
```

### 2. Run

```bash
java -jar target/esios-pipeline.jar
```

The pipeline starts, polls indicator 1293 from the ESIOS API every 10 minutes, and persists each value as an event in the configured data lake.

### 3. Configure paths

Edit `src/main/resources/pipeline.yaml` to change the inbox and data lake locations:

```yaml
infrastructure:
  inbox:
    type: FILE_SYSTEM
    configuration:
      path: "/your/inbox/path"

  dataLake:
    type: FILE_SYSTEM
    configuration:
      path: "/your/datalake/path"
```

---

## Observability

The application exports traces, metrics, and logs via **OpenTelemetry**. By default it targets a local collector:

| Signal  | Endpoint                               |
|---------|----------------------------------------|
| Logs    | `http://localhost:3100/otlp/v1/logs`   |
| Metrics | `http://localhost:4320/v1/metrics`     |
| Traces  | `http://localhost:4318/v1/traces`      |

Compatible with any OTLP-capable backend (Grafana, Jaeger, Prometheus, etc.).

---

## Context

This project is part of the **Spool Framework** Labs collection. Spool is a Java framework for building data ingestion pipelines through YAML configuration and an extensible SPI plugin system, developed as part of a Final Degree Project.

More examples available at the [`spool-framework`](https://github.com/Spool-FRAMEWORK-Labs) organization.

---

## License

MIT
