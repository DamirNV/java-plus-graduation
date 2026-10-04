package ru.practicum.ewm.stats.analyzer.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.analyzer.service.Recommendation;
import ru.practicum.ewm.stats.analyzer.service.RecommendationService;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class RecommendationsController
        extends RecommendationsControllerGrpc
        .RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            send(
                    recommendationService.getRecommendations(
                            request.getUserId(),
                            request.getMaxResults()
                    ),
                    responseObserver
            );
        } catch (RuntimeException exception) {
            fail(responseObserver, exception);
        }
    }

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            send(
                    recommendationService.getSimilarEvents(
                            request.getEventId(),
                            request.getUserId(),
                            request.getMaxResults()
                    ),
                    responseObserver
            );
        } catch (RuntimeException exception) {
            fail(responseObserver, exception);
        }
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            send(
                    recommendationService.getInteractionCounts(
                            request.getEventIdList()
                    ),
                    responseObserver
            );
        } catch (RuntimeException exception) {
            fail(responseObserver, exception);
        }
    }

    private void send(
            List<Recommendation> recommendations,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        for (Recommendation recommendation : recommendations) {
            responseObserver.onNext(
                    RecommendedEventProto
                            .newBuilder()
                            .setEventId(
                                    recommendation.eventId()
                            )
                            .setScore(
                                    recommendation.score()
                            )
                            .build()
            );
        }

        responseObserver.onCompleted();
    }

    private void fail(
            StreamObserver<RecommendedEventProto> responseObserver,
            RuntimeException exception
    ) {
        responseObserver.onError(
                Status.INTERNAL
                        .withDescription(exception.getMessage())
                        .withCause(exception)
                        .asRuntimeException()
        );
    }
}
