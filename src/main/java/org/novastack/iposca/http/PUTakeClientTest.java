package org.novastack.iposca.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.novastack.iposca.stock.Stock;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/* A test class to demonstrate how PU would make a request to remove stock from our local inventory to reflect a successful
* order.*/
public class PUTakeClientTest {
    static void main() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        //Use PUStockRequest to enter the item id and the quantity to purchase.
        Stock.PUStockRequest payload = new Stock.PUStockRequest(1, 10);
        //Wrap the payload in a string JSON object and send it to CA.
        String json = mapper.writeValueAsString(payload);

        //Send a POST request to the CA endpoint
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8088/stock/take"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();


        //Read the response from CA
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Raw JSON: " + response.body());
    }
}
