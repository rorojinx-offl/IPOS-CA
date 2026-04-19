package org.novastack.iposca.ord.factory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderInvoiceReportFactory {
    private static final String TEMPLATE_PATH = "/jasper/order/orderInvoiceMock.jrxml";
    private static final Path OUTPUT_DIR = Path.of("generated-reports", "order-invoices");

    public static File generateInvoicePdf(InvoiceHeader header, List<InvoiceLine> lines) throws IOException, JRException {
        InputStream jrxml = OrderInvoiceReportFactory.class.getResourceAsStream(TEMPLATE_PATH);
        if (jrxml == null) {
            throw new IOException("Report template not found: " + TEMPLATE_PATH);
        }

        JasperReport report = JasperCompileManager.compileReport(jrxml);

        Map<String, Object> params = new HashMap<>();
        params.put("INVOICE_NO", header.invoiceNo());
        params.put("ORDER_NO", header.orderNo());
        params.put("ISSUED_AT", header.issuedAt());
        params.put("MERCHANT_ID", header.merchantId());
        params.put("MERCHANT_NAME", header.merchantName());
        params.put("MERCHANT_USERNAME", header.merchantUsername());
        params.put("MERCHANT_STATUS", header.merchantStatus());
        params.put("SUBTOTAL", header.subtotal());
        params.put("VAT", header.vat());
        params.put("GRAND_TOTAL", header.grandTotal());
        params.put("GENERATED_AT", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(lines);
        JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);

        Files.createDirectories(OUTPUT_DIR);
        String safeInvoiceNo = header.invoiceNo().replaceAll("[^A-Za-z0-9_-]", "_");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.ROOT));
        Path pdfPath = OUTPUT_DIR.resolve("invoice_" + safeInvoiceNo + "_" + timestamp + ".pdf");
        JasperExportManager.exportReportToPdfFile(print, pdfPath.toString());
        return pdfPath.toFile();
    }

    public static class InvoiceHeader {
        private final String invoiceNo;
        private final String orderNo;
        private final String issuedAt;
        private final int merchantId;
        private final String merchantName;
        private final String merchantUsername;
        private final String merchantStatus;
        private final double subtotal;
        private final double vat;
        private final double grandTotal;

        public InvoiceHeader(String invoiceNo, String orderNo, String issuedAt, int merchantId, String merchantName,
                             String merchantUsername, String merchantStatus, double subtotal, double vat,
                             double grandTotal) {
            this.invoiceNo = invoiceNo;
            this.orderNo = orderNo;
            this.issuedAt = issuedAt;
            this.merchantId = merchantId;
            this.merchantName = merchantName;
            this.merchantUsername = merchantUsername;
            this.merchantStatus = merchantStatus;
            this.subtotal = subtotal;
            this.vat = vat;
            this.grandTotal = grandTotal;
        }

        public String invoiceNo() {
            return invoiceNo;
        }

        public String orderNo() {
            return orderNo;
        }

        public String issuedAt() {
            return issuedAt;
        }

        public int merchantId() {
            return merchantId;
        }

        public String merchantName() {
            return merchantName;
        }

        public String merchantUsername() {
            return merchantUsername;
        }

        public String merchantStatus() {
            return merchantStatus;
        }

        public double subtotal() {
            return subtotal;
        }

        public double vat() {
            return vat;
        }

        public double grandTotal() {
            return grandTotal;
        }
    }

    public static class InvoiceLine {
        private final String itemId;
        private final String description;
        private final int quantity;
        private final double unitCost;
        private final double total;

        public InvoiceLine(String itemId, String description, int quantity, double unitCost, double total) {
            this.itemId = itemId;
            this.description = description;
            this.quantity = quantity;
            this.unitCost = unitCost;
            this.total = total;
        }

        public String getItemId() {
            return itemId;
        }

        public String getDescription() {
            return description;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitCost() {
            return unitCost;
        }

        public double getTotal() {
            return total;
        }
    }
}
