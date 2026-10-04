package ru.practicum.stats.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc
            .RecommendationsControllerBlockingStub client;

    public Stream<RecommendedEventProto> getRecommendationsForUser(
            long userId,
            int maxResults
    ) {
        UserPredictionsRequestProto request =
                UserPredictionsRequestProto.newBuilder()
                        .setUserId(userId)
                        .setMaxResults(maxResults)
                        .build();

        return asStream(
                client.getRecommendationsForUser(request)
        );
    }

    public Stream<RecommendedEventProto> getSimilarEvents(
            long eventId,
            long userId,
            int maxResults
    ) {
        SimilarEventsRequestProto request =
                SimilarEventsRequestProto.newBuilder()
                        .setEventId(eventId)
                        .setUserId(userId)
                        .setMaxResults(maxResults)
                        .build();

        return asStream(
                client.getSimilarEvents(request)
        );
    }

    public Stream<RecommendedEventProto> getInteractionsCount(
            Collection<Long> eventIds
    ) {
        InteractionsCountRequestProto request =
                InteractionsCountRequestProto.newBuilder()
                        .addAllEventId(eventIds)
                        .build();

        return asStream(
                client.getInteractionsCount(request)
        );
    }

    public Map<Long, Double> getInteractionsCountMap(
            Collection<Long> eventIds
    ) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> result = new LinkedHashMap<>();

        getInteractionsCount(eventIds)
                .forEach(item ->
                        result.put(
                                item.getEventId(),
                                item.getScore()
                        )
                );

        return result;
    }

    private Stream<RecommendedEventProto> asStream(
            Iterator<RecommendedEventProto> iterator
    ) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        iterator,
                        Spliterator.ORDERED
                ),
                false
        );
    }
}
