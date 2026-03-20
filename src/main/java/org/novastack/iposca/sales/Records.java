package org.novastack.iposca.sales;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Records {
    public record Sale(int saleID, int customerID, int cardID, String paymentMethod, LocalDateTime saleDateTime, float saleAmount) {}
    public record SaleItem(int saleItemID, int saleID, int productID, int quantity, float price, float subtotal) {}
    public record Card(int cardID, int customerID, String cardVendor, String first4, String last4, String cardExp) {}
    public record SaleAggregate(Sale sale, ArrayList<SaleItem> items, Card card) {}
}
