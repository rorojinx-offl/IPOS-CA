package org.novastack.iposca.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.stock.StockEnums;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

public class HTTPClientTest {
    static void main() throws JsonProcessingException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Stock.SAStockRequest payload = new Stock.SAStockRequest(
                "Paracetamol",
                StockEnums.PackageType.BOX.name(),
                StockEnums.UnitType.CAPS.name(),
                10,
                5f,
                3,
                16,
                40
                );
        String json = mapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8088/stock/mutate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
    }
}
