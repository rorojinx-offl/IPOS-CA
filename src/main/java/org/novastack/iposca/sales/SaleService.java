package org.novastack.iposca.sales;

import javafx.collections.ObservableList;
import org.jooq.DSLContext;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.cust.customer.CustomerMonthlySpend;
import org.novastack.iposca.cust.plans.FlexiDiscountPlan;
import org.novastack.iposca.sales.UIControllers.SaleLine;
import org.novastack.iposca.utils.db.JooqConnection;
import static schema.tables.Sale.SALE;
import static schema.tables.SaleItem.SALE_ITEM;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaleService {
    public record Sale(Integer saleID, Integer customerID, String paymentMethod, String cardVendor, String cardFirst4, String cardLast4, YearMonth cardExp, LocalDateTime saleDateTime, float saleAmount) {}
    public record SaleItem(Integer saleItemID, Integer saleID, int productID, int quantity, float price, float subtotal) {}
    public record SaleDraft(Integer customerID, ArrayList<SaleItem> items, float totalAmount, float totalWithTax, float grandTotal) {}
    public record Callback(Customer customer, ObservableList<SaleLine> cartSession, CartMode mode) {}
    public enum CartMode {
        MEMBER, GUEST
    }

    public static int recordSale(Sale sale) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.insertInto(SALE)
                .set(SALE.CUST_ID, sale.customerID())
                .set(SALE.PAYMENT_METHOD, sale.paymentMethod())
                .set(SALE.CARD_VENDOR, sale.cardVendor())
                .set(SALE.CARD_FIRST_4, sale.cardFirst4())
                .set(SALE.CARD_LAST_4, sale.cardLast4())
                .set(SALE.CARD_EXP, parseYearMonth(sale.cardExp()))
                .set(SALE.SALE_DATE_TIME, parseDateTime(sale.saleDateTime().withSecond(0).withNano(0)))
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

    public static Map<Sale, ArrayList<SaleItem>> getSaleItems(int customerID, YearMonth month) {
        DSLContext ctx = JooqConnection.getDSLContext();
        LocalDateTime start = month.atDay(1).atStartOfDay().withSecond(0).withNano(0);
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay().withSecond(0).withNano(0);
        Map<Sale, ArrayList<SaleItem>> saleMap = new HashMap<>();

        ArrayList<Sale> sales = new ArrayList<>();
        ctx.selectFrom(SALE)
                .where(SALE.CUST_ID.eq(customerID))
                .and(SALE.SALE_DATE_TIME.ge(parseDateTime(start)))
                .and(SALE.SALE_DATE_TIME.lt(parseDateTime(end)))
                .orderBy(SALE.SALE_DATE_TIME.asc())
                .fetch().forEach(record -> {
                    sales.add(new Sale(
                            record.getId(),
                            record.getCustId(),
                            record.getPaymentMethod(),
                            record.getCardVendor() == null ? "" : record.getCardVendor(),
                            record.getCardFirst_4() == null ? "" : record.getCardFirst_4(),
                            record.getCardLast_4() == null ? "" : record.getCardLast_4(),
                            record.getCardExp() == null ? null : YearMonth.parse(record.getCardExp()),
                            LocalDateTime.parse(record.getSaleDateTime()),
                            record.getAmount()
                    ));
                });
        if (sales.isEmpty()) {
            System.out.println("Error with retrieving sale");
            return null;
        }

        for (Sale sale : sales) {
            ArrayList<SaleItem> items = new ArrayList<>();
            ctx.selectFrom(SALE_ITEM)
                    .where(SALE_ITEM.SALE_ID.eq(sale.saleID()))
                    .fetch().forEach(record -> {
                        items.add(new SaleItem(
                                record.getId(),
                                record.getSaleId(),
                                record.getProductId(),
                                record.getQuantity(),
                                record.getPrice(),
                                record.getSubtotal()
                        ));
                    });
            saleMap.put(sale, items);
        }
        return saleMap;
    }

    public static void checkFlexiRateChange(Customer customer, float transactionAmount) {
        if (customer.getDiscountPlan().equals(CustomerEnums.DiscountPlan.FLEXIBLE.name())) {
            int id = customer.getCustomerID();
            CustomerMonthlySpend cms = new CustomerMonthlySpend(id, YearMonth.now(), transactionAmount);
            cms.recordSpend(cms);

            //Check if the new spend rate warrants a change in the flexi rate
            CustomerMonthlySpend.FlexiRateChange fxc = cms.warrantsRateChange(id);
            if (fxc.needChange()) {
                FlexiDiscountPlan fdp = new FlexiDiscountPlan(id, fxc.rate());
                fdp.modifyRate(fdp);
            }
        }
    }

    private static String parseDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toString();
    }

    private static String parseYearMonth(YearMonth yearMonth) {
        return yearMonth == null ? null : yearMonth.toString();
    }
}
