package ru.practicum.ewm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.stats.client.AnalyzerClient;
import ru.practicum.stats.client.CollectorClient;
import ru.practicum.stats.client.StatsClient;

@Configuration
public class StatsClientConfig {

    @Bean
    public StatsClient statsClient(
            DiscoveryClient discoveryClient
    ) {
        return new StatsClient(discoveryClient);
    }

    @Bean
    public CollectorClient collectorClient() {
        return new CollectorClient();
    }

    @Bean
    public AnalyzerClient analyzerClient() {
        return new AnalyzerClient();
    }

    @Bean
    public String appName(
            @Value("${spring.application.name:event-service}")
            String appName
    ) {
        return appName;
    }
}
