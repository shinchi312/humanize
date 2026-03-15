package com.humanize.reader;

import com.humanize.kafka.KafkaTopics;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/service")
public class ServiceInfoController {

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", appName,
                "status", "ok",
                "kafkaTopics", KafkaTopics.ALL_TOPICS
        );
    }
}
