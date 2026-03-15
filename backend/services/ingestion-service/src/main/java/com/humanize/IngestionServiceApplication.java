package com.humanize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = "com.humanize")
public class IngestionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IngestionServiceApplication.class, args);
    }
}
