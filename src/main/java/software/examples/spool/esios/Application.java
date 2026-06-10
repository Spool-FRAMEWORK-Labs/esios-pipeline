package software.examples.spool.esios;

import software.spool.dsl.SpoolNodeDSL;
import software.spool.runtime.OpenTelemetryConfiguration;
import software.spool.runtime.SpoolRuntime;

import java.io.IOException;

public class Application {
    private final SpoolRuntime runtime;

    public Application() throws IOException {
        this.runtime = SpoolRuntime.builder()
                .OpenTelemetryConfiguration(OpenTelemetryConfiguration.builder()
                    .serviceName("ESIOSExamplePipeline")
                    .logsEndpoint("http://localhost:3100/otlp/v1/logs")
                    .metricsEndpoint("http://localhost:4320/v1/metrics")
                    .tracesEndpoint("http://localhost:4318/v1/traces")
                    .build())
                .withNode(SpoolNodeDSL.fromDescriptor("/pipeline.yaml"))
                .build();
    }

    public void run() {
        runtime.start();
    }
}
