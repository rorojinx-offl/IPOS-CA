package http;

public class GreetingService {
    public record GreetingRequest(String name) {}

    public static String greet(String name) {
        if (name == null || name.isBlank()) {
            return "Hello, stranger";
        }
        return "Hello, " + name;
    }
}