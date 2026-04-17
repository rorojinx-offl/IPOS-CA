package org.novastack.iposca.cust.statement;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.novastack.iposca.Bootstrap;
import org.novastack.iposca.PDF;
import org.novastack.iposca.cust.reminders.ReminderInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class StatementFactory {
    public static void generateStatement(StatementService.StatementInfo info, YearMonth month, ReminderInfo.Merchant merchant) throws IOException, JRException {
        InputStream jrxml = StatementFactory.class.getResourceAsStream("/jasper/cust/statement.jrxml");
        if (jrxml == null) {
            throw new IOException("Resource not found!");
        }

        JasperReport statement = JasperCompileManager.compileReport(jrxml);
        Map<String, Object> params = buildParams(info, month, merchant);
        JasperPrint print = JasperFillManager.fillReport(statement, params, new JREmptyDataSource(1));

        Path pdf = Path.of(Bootstrap.getDocsPath("statements").toString(), "statement-" + month.toString() + "-" + info.customer().getName() + ".pdf");
        JasperExportManager.exportReportToPdfFile(print, pdf.toString());
        PDF.openPDF(pdf.toFile());
    }

    private static Map<String, Object> buildParams(StatementService.StatementInfo info, YearMonth month, ReminderInfo.Merchant merchant) {
        Map<String, Object> params = new HashMap<>();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(info.items());

        params.put("M_NAME", merchant.name());
        params.put("M_ADDRESS", merchant.address());
        params.put("EMAIL", merchant.email());
        if (merchant.logo() != null) {
            params.put("LOGO", new ByteArrayInputStream(merchant.logo()));
        }

        params.put("C_NAME", info.customer().getName());
        params.put("C_ADDRESS", info.customer().getAddress());
        params.put("PHONE", info.customer().getPhone());
        params.put("BALANCE", info.balance());
        params.put("ITEM_DATA_SOURCE", dataSource);
        params.put("BILLING_PERIOD", month);

        return params;
    }
}
