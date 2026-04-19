package org.novastack.iposca.sales;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.novastack.iposca.Bootstrap;
import org.novastack.iposca.PDF;
import org.novastack.iposca.config.AppConfig;
import org.novastack.iposca.config.AppConfigAPI;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.cust.plans.FixedDiscountPlan;
import org.novastack.iposca.cust.plans.FlexiDiscountPlan;
import org.novastack.iposca.cust.reminders.ReminderInfo;
import org.novastack.iposca.sales.UIControllers.SelectController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for generating invoices using JasperReports.
 * */
public class InvoiceFactory {
    /**
     * Record class for bundling data required to generate an invoice.
     * @param items The items bought in the sale.
     * @param customer The customer who bought the items.
     * @param totals The total amount of the sale in 3 different categories: subtotal, VAT, and discount.
     * @param cartMode The mode of the cart (member or guest).
     * */
    public record InvoiceData(ArrayList<InvoiceItems> items, Customer customer, SelectController.Totals totals, SaleService.CartMode cartMode) {}

    /**
     * Factory method for generating invoices using JasperReports. This method can either produce a member invoice or
     * a guest invoice depending on the cart mode. The difference between the two is the presence of the customer's
     * details.
     * @param data The data required to generate the invoice.
     * @param merchant The merchant's details.
     * @throws IOException If there is an error reading the JasperReport schema.
     * @throws JRException If there is an error in the JasperReport compilation.
     * */
    public static void generateInvoice(InvoiceData data, ReminderInfo.Merchant merchant) throws IOException, JRException {
        InputStream jrxml = data.cartMode() == SaleService.CartMode.MEMBER ? InvoiceFactory.class.getResourceAsStream("/jasper/sales/purchaseInvoice.jrxml") : InvoiceFactory.class.getResourceAsStream("/jasper/sales/guestInvoice.jrxml");
        if (jrxml == null) {
            throw new IOException("Resource not found!");
        }

        JasperReport invoice = JasperCompileManager.compileReport(jrxml);
        Map<String, Object> params = buildParams(data.items(), data.customer(), data.cartMode(), merchant, data.totals());
        JasperPrint print = JasperFillManager.fillReport(invoice, params, new JREmptyDataSource(1));

        Path pdf;
        if (data.cartMode() == SaleService.CartMode.GUEST && data.customer() == null) {
            pdf = Path.of(Bootstrap.getDocsPath("ginvoice").toString(), "invoice-" + LocalDate.now().toString() + ".pdf");
        } else {
            pdf = Path.of(Bootstrap.getDocsPath("cinvoice").toString(), "invoice-" + LocalDate.now().toString() + "-" + data.customer().getName() + ".pdf");
        }
        JasperExportManager.exportReportToPdfFile(print, pdf.toString());
        PDF.openPDF(pdf.toFile());
    }

    /**
     * The parent method for building the parameters for the JasperReport. This method is responsible for deciding which
     * parameter-building method to use based on the cart mode. The reason for this is that the parameters for both schemas
     * differ slightly.
     * @param items The items bought in the sale.
     * @param customer The customer who bought the items (if applicable).
     * @param cartMode The mode of the cart (member or guest).
     * @param merchant The merchant's details.
     * @param totals The total amount of the sale in 3 different categories: subtotal, VAT, and discount.
     * @return A {@link Map} of parameter names ({@link String}) to their respective values ({@link Object}).
     * */
    private static Map<String, Object> buildParams(ArrayList<InvoiceItems> items, Customer customer, SaleService.CartMode cartMode, ReminderInfo.Merchant merchant, SelectController.Totals totals) {
        switch (cartMode) {
            case MEMBER -> {
                return buildParamsForCust(items, customer, merchant, totals);
            }
            case GUEST -> {
                return buildParamsForGuest(items, merchant, totals);
            }
            default -> throw new IllegalArgumentException("Invalid cart mode!");
        }
    }

    /**
     * The child method for building the parameters for the member invoice schema.
     * @param items The items bought in the sale.
     * @param customer The customer who bought the items.
     * @param merchant The merchant's details.
     * @param totals The total amount of the sale in 3 different categories: subtotal, VAT, and discount.
     * @return A {@link Map} of parameter names ({@link String}) to their respective values ({@link Object}).
     * */
    private static Map<String, Object> buildParamsForCust(ArrayList<InvoiceItems> items, Customer customer, ReminderInfo.Merchant merchant, SelectController.Totals totals) {
        Map<String, Object> params = new HashMap<>();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);
        int rate = customer.getDiscountPlan().equals(CustomerEnums.DiscountPlan.FIXED.name())
                ? new FixedDiscountPlan().getCurrentDiscountRate(customer.getCustomerID()) :
                new FlexiDiscountPlan().getCurrentDiscountRate(customer.getCustomerID());

        params.put("M_NAME", merchant.name());
        params.put("M_ADDRESS", merchant.address());
        params.put("EMAIL", merchant.email());
        if (merchant.logo() != null) {
            params.put("LOGO", new ByteArrayInputStream(merchant.logo()));
        }

        params.put("C_NAME", customer.getName());
        params.put("C_ADDRESS", customer.getAddress());
        params.put("PHONE", customer.getPhone());
        params.put("TOTAL", totals.sum());
        params.put("VAT_RATE", AppConfigAPI.decodeByteToInt(AppConfig.get(AppConfig.ConfigKey.VAT)));
        params.put("VAT", totals.vat());
        params.put("DSC_RATE", rate);
        params.put("DSC", totals.discount());
        params.put("ITEM_DATA_SOURCE", dataSource);

        return params;
    }

    /**
     * The child method for building the parameters for the guest invoice schema.
     * @param items The items bought in the sale.
     * @param merchant The merchant's details.
     * @param totals The total amount of the sale in 3 different categories: subtotal, VAT, and discount.
     * @return A {@link Map} of parameter names ({@link String}) to their respective values ({@link Object}).
     * */
    private static Map<String, Object> buildParamsForGuest(ArrayList<InvoiceItems> items, ReminderInfo.Merchant merchant, SelectController.Totals totals) {
        Map<String, Object> params = new HashMap<>();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);

        params.put("M_NAME", merchant.name());
        params.put("M_ADDRESS", merchant.address());
        params.put("EMAIL", merchant.email());
        if (merchant.logo() != null) {
            params.put("LOGO", new ByteArrayInputStream(merchant.logo()));
        }
        params.put("TOTAL", totals.sum());
        params.put("VAT_RATE", AppConfigAPI.decodeByteToInt(AppConfig.get(AppConfig.ConfigKey.VAT)));
        params.put("VAT", totals.vat());
        params.put("ITEM_DATA_SOURCE", dataSource);

        return params;
    }
}
