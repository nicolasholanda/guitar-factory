package com.guitarfactory.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
class GuitarControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    private Long guitarId;
    private String serialNumber;

    @BeforeEach
    void createGuitarViaOrder() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String orderJson = """
                {
                    "customerName": "Test User",
                    "customerEmail": "test@example.com",
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
                        "color": "Black"
                    }
                }
                """;
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/orders", new HttpEntity<>(orderJson, headers), String.class);
        JsonNode body = objectMapper.readTree(resp.getBody());
        guitarId = body.get("guitar").get("id").asLong();
        serialNumber = body.get("guitar").get("serialNumber").asText();
    }

    @Test
    void findAll_returnsOkWithNonEmptyList() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/guitars", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.isArray()).isTrue();
        assertThat(body.size()).isPositive();
    }

    @Test
    void findById_returnsGuitarWhenExists() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/guitars/" + guitarId, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").asLong()).isEqualTo(guitarId);
        assertThat(body.get("status").asText()).isEqualTo("ORDERED");
        assertThat(body.get("spec")).isNotNull();
        assertThat(body.get("spec").get("bodyWood").asText()).isEqualTo("ALDER");
    }

    @Test
    void findById_returnsNotFoundWhenGuitarDoesNotExist() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/guitars/999999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void findBySerialNumber_returnsGuitarWhenExists() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/guitars/serial/" + serialNumber, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("serialNumber").asText()).isEqualTo(serialNumber);
    }

    @Test
    void findBySerialNumber_returnsNotFoundForUnknownSerial() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/guitars/serial/GF-0000-UNKNOWN1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void findAll_filteredByStatus_returnsOnlyMatchingGuitars() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/guitars?status=ORDERED", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.isArray()).isTrue();
        body.forEach(guitar ->
                assertThat(guitar.get("status").asText()).isEqualTo("ORDERED"));
    }
}
