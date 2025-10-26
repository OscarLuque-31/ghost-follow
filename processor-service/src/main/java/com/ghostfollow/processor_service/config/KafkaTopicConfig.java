package com.ghostfollow.processor_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Tópico para la lista cruda de seguidores (Input del Processor)
    @Bean
    public NewTopic rawFollowerTopic() {
        return TopicBuilder.name("raw-follower-lists")
                .partitions(1)
                .replicas(1)
                .build();
    }

    // Tópico para eventos de cambio (Output del Processor al Notifier)
    @Bean
    public NewTopic changeEventsTopic() {
        return TopicBuilder.name("follower-change-events")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
