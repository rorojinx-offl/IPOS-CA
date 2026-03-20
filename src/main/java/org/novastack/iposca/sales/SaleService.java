package org.novastack.iposca.sales;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;

public class SaleService {
    public record Sale(Integer saleID, Integer customerID, String paymentMethod, String cardVendor, String cardFirst4, String cardLast4, YearMonth cardExp, LocalDateTime saleDateTime, float saleAmount) {}
    public record SaleItem(Integer saleItemID, Integer saleID, int productID, int quantity, float price, float subtotal) {}
    public record SaleDraft(Integer customerID, ArrayList<SaleItem> items, float totalAmount) {}
    public record SaleAggregate(Sale sale, ArrayList<SaleItem> items) {}
}
