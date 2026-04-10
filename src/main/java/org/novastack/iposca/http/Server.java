package org.novastack.iposca.http;

import io.javalin.Javalin;
import org.novastack.iposca.stock.Stock;

import java.util.Map;

public class Server {
    public static void start() {
        Javalin app = Javalin.create(config -> {
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
}
