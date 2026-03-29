package http;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class HTTPClient {
    private record GreetingResponse(String message) {}

    static void main() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        /*HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8088/hello/Rohit"))
                .GET()
                .build();*/

        Stock.MutateStockRequest payload = new Stock.MutateStockRequest(1, 10);
        String json = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8088/stock/mutate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Raw JSON: " + response.body());

        /*GreetingResponse greeting = mapper.readValue(response.body(), GreetingResponse.class);
        System.out.println("Message from server: " + greeting.message());*/
    }
}
