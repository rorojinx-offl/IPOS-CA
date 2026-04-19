package org.novastack.iposca.ord.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderSaFacade {
    private final OrderSaApiClient saApi = new OrderSaApiClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void authenticate(String email, String password) throws IOException, InterruptedException {
        JsonNode json = saApi.login(email, password);
        String token = require(readAny(json, "sessionToken"));
        String role = text(json, "role");
        String merchantId = require(readAny(json, "merchantId", "merchant_id"));
        RealOrderSession.set(email, role, merchantId, token);
    }

    public BalanceDto getBalance() throws IOException, InterruptedException {
        JsonNode json = saApi.getMerchantBalance(merchantId(), token());
        return new BalanceDto(
                text(json, "balance", "outstandingBalance", "outstanding_balance"),
                text(json, "accountStatus", "account_status", "status")
        );
    }

    public List<ProductDto> getProducts() throws IOException, InterruptedException {
        JsonNode json = saApi.listProducts(token());
        List<ProductDto> out = new ArrayList<>();
        for (JsonNode p : list(json, "products")) {
            out.add(new ProductDto(
                    text(p, "product_id", "productId", "id"),
                    text(p, "name", "description"),
                    number(p, "unit_price", "unitPrice", "price"),
                    intNumber(p, "stock_level", "stockLevel", "stock"),
                    intNumber(p, "minimum_stock_level", "minimumStockLevel", "minimum_stock")
            ));
        }
        return out;
    }

    public OrderSubmitDto placeOrder(List<OrderItemDto> items) throws IOException, InterruptedException {
        List<Map<String, Object>> payload = new ArrayList<>();
        for (OrderItemDto item : items) {
            payload.add(Map.of("productId", item.productId(), "quantity", item.quantity()));
        }
        JsonNode json = saApi.createOrder(merchantId(), payload, token());
        return new OrderSubmitDto(
                text(json, "orderId", "order_id", "id"),
                text(json, "totalAmount", "total_amount", "total")
        );
    }

    public List<OrderDto> getOrders() throws IOException, InterruptedException {
        JsonNode json = saApi.listOrders(merchantId(), token());
        List<OrderDto> out = new ArrayList<>();
        for (JsonNode o : list(json, "orders")) {
            out.add(new OrderDto(
                    text(o, "order_id", "orderId", "id"),
                    text(o, "order_date", "created_at", "createdAt", "date"),
                    text(o, "status", "order_status"),
                    text(o, "total_amount", "totalAmount", "total")
            ));
        }
        return out;
    }

    public List<InvoiceDto> getInvoices() throws IOException, InterruptedException {
        JsonNode json = saApi.listInvoices(merchantId(), token());
        List<InvoiceDto> out = new ArrayList<>();
        for (JsonNode i : list(json, "invoices")) {
            out.add(new InvoiceDto(
                    text(i, "invoice_id", "invoiceId", "id"),
                    text(i, "order_id", "orderId"),
                    text(i, "merchant_id", "merchantId"),
                    text(i, "invoice_date", "created_at", "date"),
                    text(i, "amount_due", "total_amount", "grand_total", "amountDue")
            ));
        }
        return out;
    }

    public String getInvoiceDetails(String invoiceId) throws IOException, InterruptedException {
        JsonNode json = saApi.getInvoice(invoiceId, token());
        if (json.hasNonNull("printableText")) {
            return json.get("printableText").asText();
        }
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    private String merchantId() throws IOException {
        String merchantId = RealOrderSession.getMerchantId();
        if (merchantId == null || merchantId.isBlank()) {
            throw new IOException("No active IPOS-SA merchant session.");
        }
        return merchantId;
    }

    private String token() throws IOException {
        String token = RealOrderSession.getSessionToken();
        if (token == null || token.isBlank()) {
            throw new IOException("No active IPOS-SA session token.");
        }
        return token;
    }

    private List<JsonNode> list(JsonNode json, String key) {
        JsonNode node = json.path(key);
        if (!node.isArray()) {
            return List.of();
        }
        List<JsonNode> out = new ArrayList<>();
        node.forEach(out::add);
        return out;
    }

    private JsonNode readAny(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.hasNonNull(key)) return node.get(key);
        }
        return null;
    }

    private String text(JsonNode node, String... keys) {
        JsonNode n = readAny(node, keys);
        return n == null ? "" : n.asText("");
    }

    private int intNumber(JsonNode node, String... keys) {
        JsonNode n = readAny(node, keys);
        return n == null ? 0 : n.asInt(0);
    }

    private double number(JsonNode node, String... keys) {
        JsonNode n = readAny(node, keys);
        return n == null ? 0.0 : n.asDouble(0.0);
    }

    private String require(JsonNode node) throws IOException {
        if (node == null || node.asText("").isBlank()) {
            throw new IOException("Missing required field in IPOS-SA response.");
        }
        return node.asText();
    }

    public record BalanceDto(String balance, String status) {}
    public record ProductDto(String productId, String name, double unitPrice, int stockLevel, int minimumStockLevel) {}
    public record OrderItemDto(String productId, int quantity) {}
    public record OrderSubmitDto(String orderId, String total) {}
    public record OrderDto(String orderId, String orderDate, String status, String total) {}
    public record InvoiceDto(String invoiceId, String orderId, String merchantId, String invoiceDate, String amountDue) {}
}
