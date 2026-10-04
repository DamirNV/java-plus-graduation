package ru.practicum.stats.client;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void sendUserAction(
            long userId,
            long eventId,
            ActionTypeProto actionType
    ) {
        sendUserAction(
                userId,
                eventId,
                actionType,
                Instant.now()
        );
    }

    public void sendUserAction(
            long userId,
            long eventId,
            ActionTypeProto actionType,
            Instant timestamp
    ) {
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(protoTimestamp)
                .build();

        client.collectUserAction(request);
    }
}
