package ru.practicum.stats.client;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
public class StatsClient {

    private static final String STATS_SERVICE_ID = "stats-server";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ParameterizedTypeReference<List<ViewStatsDto>> STATS_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;

    public StatsClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        this.restClient = RestClient.create();
    }

    public void hit(EndpointHitDto hit) {
        restClient.post()
                .uri(makeUri("/hit"))
                .body(hit)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUri(makeUri("/stats"))
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uriBuilder.queryParam("uris", uris);
        }

        List<ViewStatsDto> body = restClient.get()
                .uri(uriBuilder.build().encode().toUri())
                .retrieve()
                .body(STATS_LIST);

        return body == null ? Collections.emptyList() : body;
    }

    private URI makeUri(String path) {
        ServiceInstance instance = getStatsServerInstance();

        return UriComponentsBuilder
                .fromUri(instance.getUri())
                .path(path)
                .build()
                .toUri();
    }

    private ServiceInstance getStatsServerInstance() {
        List<ServiceInstance> instances =
                discoveryClient.getInstances(STATS_SERVICE_ID);

        if (instances.isEmpty()) {
            throw new IllegalStateException(
                    "Сервис статистики не найден в Eureka: " + STATS_SERVICE_ID
            );
        }

        return instances.getFirst();
    }
}