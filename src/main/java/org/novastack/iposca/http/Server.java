package org.novastack.iposca.http;

import io.javalin.Javalin;
import org.novastack.iposca.stock.Stock;

import java.util.Map;

/**
 * Class that handles the HTTP server that will host IPOS-CA's provided interfaces, powered by Javalin
 * */
public class Server {
    /**
     * The Javalin server instance.
     * */
    private static Javalin app;

    /**
     * Handles starting the HTTP server and the routes that it will host. The route {@code /} will return a simple
     * message indicating that the server is running. The route {@code /health} will return a simple message indicating
     * that the server is working. The route {@code /stock/take} will take a {@link Stock.PUStockRequest} as a request
     * body and subtract stock from the database. The route {@code /stock/ipos} will add/update a new item ordered from
     * IPOS. The route {@code /stock/all} will return all stock items for sale. The server uses port 8088.
     * */
    public static synchronized void start() {
        if (app != null) return;

        app = Javalin.create(config -> {
            config.routes.apiBuilder(() -> {
                config.routes.get("/", ctx -> {
                    ctx.result("Javalin is running!");
                });

                config.routes.get("/health", ctx -> {
                    ctx.result("OK");
                });

                config.routes.post("/stock/take", ctx -> {
                    Stock.PUStockRequest request = ctx.bodyAsClass(Stock.PUStockRequest.class);
                    int status = Stock.minusStock(request.id(), request.quantity());
                    ctx.json(Map.of(
                            "id", request.id(),
                            "quantityChange", request.quantity(),
                            "status", status
                    ));
                });

                config.routes.post("/stock/ipos", ctx -> {
                    Stock.SAStockRequest request = ctx.bodyAsClass(Stock.SAStockRequest.class);
                    int status = Stock.upsertIPOSItem(new Stock(request.name(), request.packageType(), request.units(), request.unitsInAPack(), request.bulkCost(), request.markupRate(), request.quantity(), request.stockLimit()));
                    ctx.json(Map.of(
                            "productName", request.name(),
                            "status", status
                    ));
                });

                config.routes.get("/stock/all", ctx -> {
                    ctx.json(Stock.getAllStockForSale());
                });
            });
        }).start(8088);

        System.out.println("Server started at http://localhost:8088");
    }

    /**
     * Stops the HTTP server. This method is necessary to prevent the server from running indefinitely, especially after
     * a JavaFX application has been closed.
     * */
    public static synchronized void stop() {
        if (app == null) return;

        app.stop();
        app = null;
        System.out.println("Server stopped");
    }
}
