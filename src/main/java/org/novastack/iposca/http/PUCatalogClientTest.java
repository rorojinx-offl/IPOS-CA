package org.novastack.iposca.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

/* A test class to demonstrate how PU would make a request to get the local inventory of a CA merchant and display it
on their service.*/
public class PUCatalogClientTest {
    private record StockResponse(int id, String name, float cost, int quantity) {}

    static void main() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        //Send a GET request to the CA endpoint
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8088/stock/all"))
                .GET()
                .build();
        //Get JSON response from CA
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Raw JSON: " + response.body());


        //Parse the JSON response by getting the body, and using Jackson to deserialize it into a StockResponse object
        ArrayList<StockResponse> stocks = mapper.readValue(response.body(), new TypeReference<ArrayList<StockResponse>>() {});
        //Test printing the catalogue
        for (StockResponse stock : stocks) {
            System.out.printf("Name: %s, Cost: %.2f, Quantity: %d\n", stock.name(), stock.cost(), stock.quantity());
        }

    }
}
