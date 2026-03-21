package org.novastack.iposca.sales;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;
import static schema.tables.Sale.SALE;
import static schema.tables.SaleItem.SALE_ITEM;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;

public class SaleService {
    public record Sale(Integer saleID, Integer customerID, String paymentMethod, String cardVendor, String cardFirst4, String cardLast4, YearMonth cardExp, LocalDateTime saleDateTime, float saleAmount) {}
    public record SaleItem(Integer saleItemID, Integer saleID, int productID, int quantity, float price, float subtotal) {}
    public record SaleDraft(Integer customerID, ArrayList<SaleItem> items, float totalAmount) {}
    public record SaleAggregate(Sale sale, ArrayList<SaleItem> items) {}

    public static int recordSale(Sale sale) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.insertInto(SALE)
                .set(SALE.CUST_ID, sale.customerID())
                .set(SALE.PAYMENT_METHOD, sale.paymentMethod())
                .set(SALE.CARD_VENDOR, sale.cardVendor())
                .set(SALE.CARD_FIRST_4, sale.cardFirst4())
                .set(SALE.CARD_LAST_4, sale.cardLast4())
                .set(SALE.CARD_EXP, parseYearMonth(sale.cardExp()))
                .set(SALE.SALE_DATE_TIME, parseDateTime(sale.saleDateTime()))
                .set(SALE.AMOUNT, sale.saleAmount())
                .returning(SALE.ID).fetchOne().getId();
    }

    public static void recordSaleItem(SaleItem saleItem) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(SALE_ITEM)
                .set(SALE_ITEM.SALE_ID, saleItem.saleID())
                .set(SALE_ITEM.PRODUCT_ID, saleItem.productID())
                .set(SALE_ITEM.QUANTITY, saleItem.quantity())
                .set(SALE_ITEM.PRICE, saleItem.price())
                .set(SALE_ITEM.SUBTOTAL, saleItem.subtotal())
                .execute();
    }

    private static String parseDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toString();
    }

    private static String parseYearMonth(YearMonth yearMonth) {
        return yearMonth == null ? null : yearMonth.toString();
    }
}
