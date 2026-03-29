package http;

import io.javalin.Javalin;

import java.util.Map;

public class Server {
    static void main() {
        Javalin app = Javalin.create(config -> {
            config.routes.apiBuilder(() -> {
                config.routes.get("/", ctx -> {
                    ctx.result("Javalin is running!");
                });

                config.routes.get("/hello/{name}", ctx -> {
                    String name = ctx.pathParam("name");
                    String message = GreetingService.greet(name);
                    ctx.json(Map.of("message", message));
                });

                config.routes.post("/hello", ctx -> {
                    GreetingService.GreetingRequest request = ctx.bodyAsClass(GreetingService.GreetingRequest.class);
                    String message = GreetingService.greet(request.name());
                    ctx.json(Map.of("message", message));
                });

                config.routes.post("/stock/mutate", ctx -> {
                    Stock.MutateStockRequest request = ctx.bodyAsClass(Stock.MutateStockRequest.class);
                    int status = Stock.mutateStock(request.id(), request.quantity());
                    ctx.json(Map.of(
                            "id", request.id(),
                            "quantityChange", request.quantity(),
                            "status", status
                            ));
                });
            });
        }).start(8088);

        System.out.println("Server started at http://localhost:8088");
    }
}
