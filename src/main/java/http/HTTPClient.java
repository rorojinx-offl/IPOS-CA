package http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HTTPClient {
    private record GreetingResponse(String message) {}
    private record StockResponse(int id, String name, float cost, int quantity) {}

    static void main() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        /*HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8088/hello/Rohit"))
                .GET()
                .build();*/

       /* Stock.MutateStockRequest payload = new Stock.MutateStockRequest(1, 10);
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8088/stock/mutate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();*/

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8088/stock/all"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Raw JSON: " + response.body());

        /*GreetingResponse greeting = mapper.readValue(response.body(), GreetingResponse.class);
        System.out.println("Message from server: " + greeting.message());*/

        ArrayList<StockResponse> stocks = mapper.readValue(response.body(), new TypeReference<ArrayList<StockResponse>>() {});
        for (StockResponse stock : stocks) {
            System.out.printf("Name: %s, Cost: %.2f, Quantity: %d\n", stock.name(), stock.cost(), stock.quantity());
        }
    }
}
