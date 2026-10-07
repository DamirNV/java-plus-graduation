package ru.practicum.ewm.stats.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.AvroMessageCodec;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Service
@RequiredArgsConstructor
public class SimilarityProducer {

    private final KafkaTemplate<Long, byte[]> kafkaTemplate;

    @Value("${stats.kafka.events-similarity-topic}")
    private String similarityTopic;

    public void send(EventSimilarityAvro similarity) {
        kafkaTemplate.send(
                similarityTopic,
                similarity.getEventA(),
                AvroMessageCodec.serialize(similarity)
        );
    }
}
