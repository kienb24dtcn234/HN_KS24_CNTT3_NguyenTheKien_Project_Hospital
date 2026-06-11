package re.hospital.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        baseUrl = "http://localhost:" + port + "/api/v1/auth";
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @DisplayName("POST /login - Success")
    void login_Success() {
        String body = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/login", new HttpEntity<>(body, jsonHeaders()), Map.class);

        assertEquals(200, response.getStatusCode().value());
        Map data = (Map) response.getBody().get("data");
        assertNotNull(data.get("accessToken"));
        assertEquals("admin", data.get("username"));
    }

    @Test
    @DisplayName("POST /login - Bad Credentials")
    void login_BadCredentials() {
        String body = "{\"username\":\"admin\",\"password\":\"wrong\"}";
        try {
            restTemplate.postForEntity(baseUrl + "/login", new HttpEntity<>(body, jsonHeaders()), Map.class);
            fail("Should throw exception");
        } catch (HttpClientErrorException e) {
            assertEquals(401, e.getStatusCode().value());
        }
    }

    @Test
    @DisplayName("POST /register - Success")
    void register_Success() {
        String body = "{\"username\":\"test" + System.currentTimeMillis() + "\","
                + "\"email\":\"test" + System.currentTimeMillis() + "@gmail.com\","
                + "\"password\":\"123456\",\"fullName\":\"Test User\"}";

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/register", new HttpEntity<>(body, jsonHeaders()), Map.class);

        assertEquals(201, response.getStatusCode().value());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    @DisplayName("POST /register - Duplicate Username")
    void register_DuplicateUsername() {
        String body = "{\"username\":\"admin\",\"email\":\"dup@gmail.com\","
                + "\"password\":\"123456\",\"fullName\":\"Dup User\"}";
        try {
            restTemplate.postForEntity(baseUrl + "/register", new HttpEntity<>(body, jsonHeaders()), Map.class);
            fail("Should throw exception");
        } catch (HttpClientErrorException e) {
            assertEquals(409, e.getStatusCode().value());
        }
    }

    @Test
    @DisplayName("POST /register - Validation Error")
    void register_ValidationError() {
        String body = "{\"username\":\"\",\"email\":\"invalid\",\"password\":\"12\",\"fullName\":\"\"}";
        try {
            restTemplate.postForEntity(baseUrl + "/register", new HttpEntity<>(body, jsonHeaders()), Map.class);
            fail("Should throw exception");
        } catch (HttpClientErrorException e) {
            assertEquals(400, e.getStatusCode().value());
        }
    }
}
