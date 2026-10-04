package ru.practicum.ewm.stats.analyzer.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.service.AnalyzerStorageService;
import ru.practicum.ewm.stats.avro.AvroMessageCodec;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final AnalyzerStorageService storageService;

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

        storageService.saveUserAction(action);
    }
}
