package software.examples.spool.esios.plugins;

import software.spool.crawler.api.port.source.PollSource;
import software.spool.infrastructure.spi.SpoolPlugin;
import software.spool.infrastructure.spi.provider.PluginConfiguration;
import software.spool.infrastructure.spi.provider.PollSourceProvider;

@SpoolPlugin(PollSourceProvider.class)
public class EsiosHTTPPollSourceProvider implements PollSourceProvider {
    @Override
    public String name() {
        return "ESIOS_HTTP";
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean supports(PluginConfiguration configuration) {
        return configuration.has("url") && configuration.has("apiKey");
    }

    @Override
    public PollSource<?> create(PluginConfiguration configuration) {
        return new EsiosHTTPPollSource(
                configuration.require("url"),
                configuration.require("apiKey"),
                configuration.require("sourceId")
        );
    }
}
