package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
public class StatsClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ParameterizedTypeReference<List<ViewStatsDto>> STATS_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient rest;

    public StatsClient(@Value("${stats-server.url:http://localhost:9090}") String baseUrl) {
        this.rest = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void hit(EndpointHitDto hit) {
        rest.post()
                .uri("/hit")
                .body(hit)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {
        List<ViewStatsDto> body = rest.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", start.format(FORMATTER))
                        .queryParam("end", end.format(FORMATTER))
                        .queryParam("unique", unique)
                        .queryParamIfPresent("uris",
                                uris == null || uris.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(uris))
                        .build())
                .retrieve()
                .body(STATS_LIST);

        return body == null ? Collections.emptyList() : body;
    }
}
