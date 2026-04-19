package org.novastack.iposca.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.stock.StockEnums;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/* A test class to demonstrate how SA would make a request to add or update our local stock records to reflect a successful
* order.*/
public class SAClientTest {
    static void main() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        //SA will create a payload and populate the data with the required info.
        Stock.SAStockRequest payload = new Stock.SAStockRequest(
                "Aspirin",
                StockEnums.PackageType.BOX.name(),
                StockEnums.UnitType.CAPS.name(),
                20,
                6.5f,
                2,
                100,
                30
                );
        //Wrap the payload in a string JSON object and send it to SA.
        String json = mapper.writeValueAsString(payload);
        //Send a POST request to the CA endpoint
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8088/stock/ipos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        //Response from CA
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Raw JSON: " + response.body());
    }
}
