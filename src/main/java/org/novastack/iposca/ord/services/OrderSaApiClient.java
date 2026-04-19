package org.novastack.iposca.ord.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class OrderSaApiClient {
    private static final String API_BASE = "http://localhost:8080/api";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public JsonNode login(String email, String password) throws IOException, InterruptedException {
        return request("POST", "/auth/login", Map.of(
                "email", email,
                "password", password
        ), null);
    }

    public JsonNode listProducts(String sessionToken) throws IOException, InterruptedException {
        return request("GET", "/products", null, sessionToken);
    }

    public JsonNode createOrder(String merchantId, List<Map<String, Object>> items, String sessionToken)
            throws IOException, InterruptedException {
        return request("POST", "/orders", Map.of(
                "merchantId", merchantId,
                "items", items
        ), sessionToken);
    }

    public JsonNode listOrders(String merchantId, String sessionToken) throws IOException, InterruptedException {
        return request("GET", "/orders?merchantId=" + merchantId, null, sessionToken);
    }

    public JsonNode listInvoices(String merchantId, String sessionToken) throws IOException, InterruptedException {
        return request("GET", "/invoices?merchantId=" + merchantId, null, sessionToken);
    }

    public JsonNode getInvoice(String invoiceId, String sessionToken) throws IOException, InterruptedException {
        return request("GET", "/invoices/" + invoiceId, null, sessionToken);
    }

    public JsonNode getMerchantBalance(String merchantId, String sessionToken) throws IOException, InterruptedException {
        return request("GET", "/merchants/" + merchantId + "/balance", null, sessionToken);
    }

    private JsonNode request(String method, String path, Object body, String sessionToken)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json");

        if (sessionToken != null && !sessionToken.isBlank()) {
            builder.header("X-Session-Token", sessionToken);
        }

        if ("POST".equals(method)) {
            String payload = body == null ? "{}" : objectMapper.writeValueAsString(body);
            builder.POST(HttpRequest.BodyPublishers.ofString(payload));
        } else {
            builder.GET();
        }

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        JsonNode json = response.body() == null || response.body().isBlank()
                ? objectMapper.createObjectNode()
                : objectMapper.readTree(response.body());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String error = json.hasNonNull("error")
                    ? json.get("error").asText()
                    : "Request failed with status " + response.statusCode();
            throw new IOException(error);
        }

        return json;
    }
}
