package com.example.authservice.events.producers;

import com.example.authservice.events.models.PermissionChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public void publish(PermissionChangedEvent event) {
        kafkaTemplate.send("permission-changed", event);
    }
}
