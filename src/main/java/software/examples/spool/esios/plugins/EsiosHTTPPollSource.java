package software.examples.spool.esios.plugins;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.exception.SpoolException;
import software.spool.core.port.logging.Logger;
import software.spool.crawler.api.port.source.PollSource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class EsiosHTTPPollSource implements PollSource<byte[]> {
    private static final Logger LOG = LoggerFactory.getLogger(EsiosHTTPPollSource.class);
    private final HttpClient httpClient;
    private final String url;
    private final String apiKey;
    private final String sourceId;

    public EsiosHTTPPollSource(String url, String apiKey, String sourceId) {
        this.url = url;
        this.apiKey = apiKey;
        this.sourceId = sourceId;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public byte[] fetch() throws SpoolException {
        try {
            LOG.info("Polling ESIOS API at {}", url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .header("x-api-key", apiKey)
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofByteArray()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        sourceId + " returned HTTP " + response.statusCode()
                );
            }

            return response.body();
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error polling: " + e.getMessage(), e);
        }
    }

    @Override
    public String sourceId() {
        return sourceId;
    }
}
