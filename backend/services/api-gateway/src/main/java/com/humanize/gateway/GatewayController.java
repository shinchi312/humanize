package com.humanize.gateway;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    @GetMapping("/routes")
    public Map<String, Object> routes() {
        return Map.of(
                "auth", "http://localhost:8081",
                "library", "http://localhost:8082",
                "ingestion", "http://localhost:8083",
                "reader", "http://localhost:8084",
                "activity", "http://localhost:8085",
                "recommendation", "http://localhost:8086",
                "notification", "http://localhost:8087",
                "ai", "http://localhost:8088"
        );
    }
}
