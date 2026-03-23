package org.novastack.iposca.sales;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.novastack.iposca.cust.UIControllers.DebtController;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.reminders.ReminderInfo;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InvoiceFactory {
    public record InvoiceData(ArrayList<InvoiceItems> items, Customer customer, float totalAmount, SaleService.CartMode cartMode) {}

    public static void generateInvoice(InvoiceData data) throws IOException, JRException, UnsupportedOperationException {
        InputStream jrxml = data.cartMode() == SaleService.CartMode.MEMBER ? InvoiceFactory.class.getResourceAsStream("/jasper/sales/purchaseInvoice.jrxml") : InvoiceFactory.class.getResourceAsStream("/jasper/sales/guestInvoice.jrxml");
        if (jrxml == null) {
            throw new IOException("Resource not found!");
        }

        ReminderInfo.Merchant merchant = new ReminderInfo.Merchant(
                "T-Pharma",
                "123 Test Street, Test Town, Testshire, TE1 1ST",
                "test@tpharma.com",
                DebtController.loadLogo());

        JasperReport reminder = JasperCompileManager.compileReport(jrxml);
        Map<String, Object> params = buildParams(data.items(), data.customer(), data.cartMode(), merchant, data.totalAmount());
        JasperPrint print = JasperFillManager.fillReport(reminder, params, new JREmptyDataSource(1));
        Files.createDirectories(Path.of("generated-reports"));
        Path pdf = Path.of("generated-reports", "invoice-" + LocalDate.now().toString() + "-" + data.customer().getName() + ".pdf");
        JasperExportManager.exportReportToPdfFile(print, pdf.toString());
        openPDF(pdf.toFile());
    }

    private static Map<String, Object> buildParams(ArrayList<InvoiceItems> items, Customer customer, SaleService.CartMode cartMode, ReminderInfo.Merchant merchant, float totalAmount) {
        switch (cartMode) {
            case MEMBER -> {
                return buildParamsForCust(items, customer, merchant, totalAmount);
            }
            case GUEST -> {
                return buildParamsForGuest(items, merchant);
            }
            default -> throw new IllegalArgumentException("Invalid cart mode!");
        }
    }

    private static Map<String, Object> buildParamsForCust(ArrayList<InvoiceItems> items, Customer customer, ReminderInfo.Merchant merchant, float totalAmount) {
        Map<String, Object> params = new HashMap<>();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);

        params.put("M_NAME", merchant.name());
        params.put("M_ADDRESS", merchant.address());
        params.put("EMAIL", merchant.email());
        if (merchant.logo() != null) {
            params.put("LOGO", new ByteArrayInputStream(merchant.logo()));
        }

        params.put("C_NAME", customer.getName());
        params.put("C_ADDRESS", customer.getAddress());
        params.put("PHONE", customer.getPhone());
        params.put("TOTAL", totalAmount);
        params.put("ITEM_DATA_SOURCE", dataSource);

        return params;
    }

    private static Map<String, Object> buildParamsForGuest(ArrayList<InvoiceItems> items, ReminderInfo.Merchant merchant) {
        return new HashMap<>();
    }

    private static void openPDF(File pdfFile) throws IOException, UnsupportedOperationException {
        if (!Desktop.isDesktopSupported()) {
            throw new UnsupportedOperationException("Desktop is not supported");
        }

        Desktop desktop = Desktop.getDesktop();

        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            throw new UnsupportedOperationException("Opening PDF files is not supported");
        }

        desktop.open(pdfFile);
    }
}
