package ru.practicum.ewm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.stats.client.AnalyzerClient;
import ru.practicum.stats.client.CollectorClient;

@Configuration
public class StatsClientConfig {

    @Bean
    public CollectorClient collectorClient() {
        return new CollectorClient();
    }

    @Bean
    public AnalyzerClient analyzerClient() {
        return new AnalyzerClient();
    }
}