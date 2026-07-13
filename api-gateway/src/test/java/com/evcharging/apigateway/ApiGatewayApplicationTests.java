package com.evcharging.apigateway;  // Changed from api_gateway to apigateway

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ApiGatewayApplication.class)  // Explicitly reference the main class
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}