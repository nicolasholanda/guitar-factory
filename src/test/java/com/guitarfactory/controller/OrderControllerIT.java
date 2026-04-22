package com.guitarfactory.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@org.springframework.test.context.ActiveProfiles("test")
class OrderControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String validOrderJson() {
        return """
                {
                    "customerName": "Jane Doe",
                    "customerEmail": "jane@example.com",
                    "modelId": 1,
                    "spec": {
                        "bodyType": "SOLID",
                        "bodyWood": "ALDER",
                        "neckWood": "MAPLE",
                        "fretboardWood": "ROSEWOOD",
                        "stringCount": "SIX",
                        "pickupType": "SINGLE_COIL",
                        "finish": "GLOSS",
                        "scaleLength": 25.50,
                        "color": "Sunburst"
                    }
                }
                """;
    }

    private JsonNode createOrder() throws Exception {
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/orders", new HttpEntity<>(validOrderJson(), jsonHeaders()), String.class);
        return objectMapper.readTree(resp.getBody());
    }

    @Test
    void createOrder_returnsCreatedWithGuitarAndSerialNumber() throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/orders", new HttpEntity<>(validOrderJson(), jsonHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").asLong()).isPositive();
        assertThat(body.get("status").asText()).isEqualTo("IN_PRODUCTION");
        assertThat(body.get("guitar")).isNotNull();
        assertThat(body.get("guitar").get("serialNumber").asText()).matches("GF-\\d{4}-[A-Z0-9]{8}");
        assertThat(body.get("guitar").get("estimatedPrice").decimalValue()).isPositive();
    }

    @Test
    void createOrder_withMissingRequiredFields_returnsBadRequest() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/orders",
                new HttpEntity<>("""
                        { "customerName": "Jane" }
                        """, jsonHeaders()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createOrder_withNonExistentModelId_returnsNotFound() {
        String body = validOrderJson().replace("\"modelId\": 1", "\"modelId\": 9999");
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/orders", new HttpEntity<>(body, jsonHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void findAll_returnsOkWithOrders() throws Exception {
        createOrder();

        ResponseEntity<String> response = restTemplate.getForEntity("/api/orders", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode list = objectMapper.readTree(response.getBody());
        assertThat(list.isArray()).isTrue();
        assertThat(list.size()).isPositive();
    }

    @Test
    void findById_returnsOrderWhenExists() throws Exception {
        long id = createOrder().get("id").asLong();

        ResponseEntity<String> response = restTemplate.getForEntity("/api/orders/" + id, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").asLong()).isEqualTo(id);
    }

    @Test
    void findById_returnsNotFoundWhenOrderDoesNotExist() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/orders/999999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void completeOrder_transitionsOrderAndGuitarToCompleted() throws Exception {
        long id = createOrder().get("id").asLong();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders/" + id + "/complete", HttpMethod.PUT,
                new HttpEntity<>(jsonHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(body.get("guitar").get("status").asText()).isEqualTo("COMPLETED");
    }

    @Test
    void deliverOrder_transitionsGuitarToDelivered() throws Exception {
        long id = createOrder().get("id").asLong();
        restTemplate.exchange("/api/orders/" + id + "/complete", HttpMethod.PUT,
                new HttpEntity<>(jsonHeaders()), String.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders/" + id + "/deliver", HttpMethod.PUT,
                new HttpEntity<>(jsonHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("guitar").get("status").asText()).isEqualTo("DELIVERED");
    }

    @Test
    void cancelOrder_returnsConflictWhenOrderIsAlreadyCompleted() throws Exception {
        long id = createOrder().get("id").asLong();
        restTemplate.exchange("/api/orders/" + id + "/complete", HttpMethod.PUT,
                new HttpEntity<>(jsonHeaders()), String.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders/" + id + "/cancel", HttpMethod.PUT,
                new HttpEntity<>(jsonHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
