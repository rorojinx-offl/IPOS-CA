package org.novastack.iposca.cust.statement;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.novastack.iposca.Bootstrap;
import org.novastack.iposca.PDF;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.cust.reminders.ReminderInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory class that generates PDF monthly statements for customer debt using JasperReports.
 * */
public class StatementFactory {
    /**
     * Generates a PDF statement for a customer debt. With the given parameters, it loads the JasperReport schema and
     * compiles it, then fills it with the built parameters, exports it to a PDF file, and opens it.
     * @param info The information required to form a statement.
     * @param month The month of the billing cycle.
     * @param merchant The merchant information.
     * @throws JRException If there is an error in the JasperReport compilation.
     * @throws IOException If there is an error in the resource loading.
     * */
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

    /**
     * Builds JasperReport parameters with the data provided by {@link #generateStatement(StatementService.StatementInfo, YearMonth, ReminderInfo.Merchant)}
     * and maps the parameters ({@link String}) to a generic {@link Object} to allow for any datatype (however, the
     * JasperReports compiler will complain if the datatype does not match the parameter type in the schema).
     * @param info The information about the customer debt.
     * @param month The month of the billing cycle.
     * @param merchant The merchant information.
     * @return A {@link Map} of parameter names ({@link String}) to their respective values ({@link Object}).
     * */
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
