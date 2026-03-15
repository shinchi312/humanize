package com.humanize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = "com.humanize")
public class ReaderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReaderServiceApplication.class, args);
    }
}
