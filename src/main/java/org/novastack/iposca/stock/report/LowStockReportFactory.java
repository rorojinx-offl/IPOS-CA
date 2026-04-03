package org.novastack.iposca.stock.report;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LowStockReportFactory {
    public record Merchant(String name, String address, String email, byte[] logo) {}
    public record ReportData(ArrayList<LowStockBean> items, Merchant merchant) {}
    private static DateTimeFormatter dateFormat =  DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void generateLowStockReport(ReportData data) throws IOException, JRException {
        InputStream jrxml = LowStockReportFactory.class.getResourceAsStream("/jasper/stock/lowStockReport.jrxml");
        if (jrxml == null) {
            throw new IOException("Report schema not found!");
        }

        JasperReport lowStockReport = JasperCompileManager.compileReport(jrxml);
        Map<String, Object> params = buildParams(data);
        JasperPrint print = JasperFillManager.fillReport(lowStockReport, params, new JREmptyDataSource(1));

        Files.createDirectories(Path.of("generated-reports"));
        Path pdf = Path.of("generated-reports", "lowStockReport-" + LocalDate.now().format(dateFormat) + ".pdf");
        JasperExportManager.exportReportToPdfFile(print, pdf.toString());
    }

    private static Map<String, Object> buildParams(ReportData data) {
        Map<String, Object> params = new HashMap<>();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data.items());

        params.put("M_NAME", data.merchant().name());
        params.put("M_ADDRESS", data.merchant().address());
        params.put("EMAIL", data.merchant().email());
        if (data.merchant().logo() != null) {
            params.put("LOGO", new ByteArrayInputStream(data.merchant().logo()));
        }
        params.put("ITEM_DATA_SOURCE", dataSource);
        params.put("GENERATED", LocalDate.now().format(dateFormat));

        return params;
    }
}
