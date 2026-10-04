package ru.practicum.ewm.stats.analyzer.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.service.AnalyzerStorageService;
import ru.practicum.ewm.stats.avro.AvroMessageCodec;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
@RequiredArgsConstructor
public class EventSimilarityConsumer {

    private final AnalyzerStorageService storageService;

    @KafkaListener(
            topics = "${stats.kafka.events-similarity-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(byte[] payload) {
        EventSimilarityAvro similarity =
                AvroMessageCodec.deserialize(
                        payload,
                        EventSimilarityAvro.class
                );

        storageService.saveSimilarity(similarity);
    }
}
