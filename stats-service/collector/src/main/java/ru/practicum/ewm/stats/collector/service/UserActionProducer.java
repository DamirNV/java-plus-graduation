package ru.practicum.ewm.stats.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.AvroMessageCodec;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor
public class UserActionProducer {

    private final KafkaTemplate<Long, byte[]> kafkaTemplate;

    @Value("${stats.kafka.user-actions-topic}")
    private String userActionsTopic;

    public void send(UserActionAvro action) {
        kafkaTemplate.send(
                userActionsTopic,
                action.getEventId(),
                AvroMessageCodec.serialize(action)
        );
    }
}
