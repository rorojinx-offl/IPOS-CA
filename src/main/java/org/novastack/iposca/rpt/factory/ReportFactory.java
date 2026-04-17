package org.novastack.iposca.rpt.factory;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.novastack.iposca.Bootstrap;
import org.novastack.iposca.rpt.model.DebtChangeData;
import org.novastack.iposca.rpt.model.StockItem;
import org.novastack.iposca.rpt.model.TurnoverData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportFactory {

    private static final String REPORT_DIR = "generated-reports";

    public static File generateTurnoverReport(TurnoverData data, String currentUser) throws IOException, JRException {
        InputStream jrxml = ReportFactory.class.getResourceAsStream("/jasper/rpt/turnoverReport.jrxml");
        if (jrxml == null) {
            throw new IOException("Report template not found: turnoverReport.jrxml");
        }

        JasperReport report = JasperCompileManager.compileReport(jrxml);

        Map<String, Object> params = new HashMap<>();
        params.put("PERIOD_START", data.getReportPeriodStart().toString());
        params.put("PERIOD_END", data.getReportPeriodEnd().toString());
        params.put("TOTAL_SALES_AMOUNT", data.getTotalSalesAmount());
        params.put("TOTAL_SALES_COUNT", data.getTotalSalesCount());
        params.put("TOTAL_ORDERS", data.getTotalOrdersPlacedValue());
        params.put("GENERATED_BY", data.getGeneratedBy());
        params.put("GENERATED_TIMESTAMP", data.getGeneratedTimestamp().toString());

        JRDataSource dataSource = data.getSales().isEmpty()
                ? new JREmptyDataSource(1)
                : new JRBeanCollectionDataSource(data.getSales());
        JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);

        //Files.createDirectories(Path.of(REPORT_DIR));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path pdf = Path.of(Bootstrap.getDocsPath("turnover").toString(), "turnover_report_" + timestamp + "_" + currentUser + ".pdf");
        JasperExportManager.exportReportToPdfFile(print, pdf.toString());

        return new File(pdf.toUri());
    }

    public static File generateDebtReport(DebtChangeData data, String currentUser) throws IOException, JRException {
        InputStream jrxml = ReportFactory.class.getResourceAsStream("/jasper/rpt/debtReport.jrxml");
        if (jrxml == null) {
            throw new IOException("Report template not found: debtReport.jrxml");
        }

        JasperReport report = JasperCompileManager.compileReport(jrxml);

        Map<String, Object> params = new HashMap<>();
        params.put("PERIOD_START", data.getStartDate().toString());
        params.put("PERIOD_END", data.getEndDate().toString());
        params.put("OPENING_DEBT", data.getOpeningAggregateDebt());
        params.put("PAYMENTS_RECEIVED", data.getPaymentsReceived());
        params.put("NEW_DEBT_ACCRUED", data.getNewDebtAccrued());
        params.put("CLOSING_DEBT", data.getClosingAggregateDebt());
        params.put("TOTAL_DEBTORS", data.getTotalDebtorsCount());
        params.put("TOTAL_PAYMENTS_COUNT", data.getTotalPaymentsCount());
        params.put("TOTAL_CREDIT_SALES", data.getTotalCreditSalesCount());
        params.put("GENERATED_BY", data.getGeneratedBy());
        params.put("GENERATED_TIMESTAMP", data.getGeneratedTimestamp().toString());

        JRDataSource dataSource = data.getPdfRows().isEmpty()
                ? new JREmptyDataSource(1)
                : new JRBeanCollectionDataSource(data.getPdfRows());
        JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);

        //Files.createDirectories(Path.of(REPORT_DIR));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        Path pdf = Path.of(Bootstrap.getDocsPath("debt").toString(), "debt_report_" + timestamp + "_" + currentUser + ".pdf");
        JasperExportManager.exportReportToPdfFile(print, pdf.toString());

        return new File(pdf.toUri());
    }

    public static File generateStockReport(List<StockItem> items, String currentUser) throws IOException, JRException {
        InputStream jrxml = ReportFactory.class.getResourceAsStream("/jasper/rpt/stockReport.jrxml");
        if (jrxml == null) {
            throw new IOException("Report template not found: stockReport.jrxml");
        }

        JasperReport report = JasperCompileManager.compileReport(jrxml);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);

        Map<String, Object> params = new HashMap<>();
        params.put("GENERATED_BY", currentUser);
        params.put("GENERATED_TIMESTAMP", LocalDate.now().toString());

        int totalUnits = items.stream().mapToInt(StockItem::getQuantity).sum();
        float totalValue = (float) items.stream().mapToDouble(StockItem::getTotalStockValue).sum();
        float totalVat = (float) items.stream().mapToDouble(StockItem::getVatAmount).sum();

        params.put("TOTAL_ITEMS", items.size());
        params.put("TOTAL_UNITS", totalUnits);
        params.put("TOTAL_VALUE", totalValue);
        params.put("TOTAL_VAT", totalVat);

        JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);

        //Files.createDirectories(Path.of(REPORT_DIR));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path pdf = Path.of(Bootstrap.getDocsPath("stockReport").toString(), "stock_report_" + timestamp + "_" + currentUser + ".pdf");
        JasperExportManager.exportReportToPdfFile(print, pdf.toString());

        return new File(pdf.toUri());
    }

}
