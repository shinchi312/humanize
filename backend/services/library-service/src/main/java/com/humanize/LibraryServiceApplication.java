package com.humanize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = "com.humanize")
public class LibraryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryServiceApplication.class, args);
    }
}
