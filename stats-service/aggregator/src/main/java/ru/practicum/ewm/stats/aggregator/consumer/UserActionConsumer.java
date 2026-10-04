package ru.practicum.ewm.stats.aggregator.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.aggregator.service.SimilarityService;
import ru.practicum.ewm.stats.avro.AvroMessageCodec;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final SimilarityService similarityService;

    @KafkaListener(
            topics = "${stats.kafka.user-actions-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(byte[] payload) {
        UserActionAvro action =
                AvroMessageCodec.deserialize(
                        payload,
                        UserActionAvro.class
                );

        similarityService.process(action);
    }
}
