package com.humanize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = "com.humanize")
public class ActivityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActivityServiceApplication.class, args);
    }
}
