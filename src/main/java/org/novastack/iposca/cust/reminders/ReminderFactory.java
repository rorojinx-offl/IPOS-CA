package org.novastack.iposca.cust.reminders;

import net.sf.jasperreports.engine.*;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.rpt.factory.ReportFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ReminderFactory {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    public static void generateReminder(ReminderInfo info, ReminderInfo.Merchant merchant, CustomerEnums.ReminderType type, Path outputPath, Path templatePath) throws JRException, IOException {
        InputStream jrxml = ReminderFactory.class.getResourceAsStream(templatePath.toString());
        if (jrxml == null) {
            throw new IOException("Resource not found: " + templatePath);
        }

        JasperReport reminder = JasperCompileManager.compileReport(jrxml);
        Map<String, Object> params = buildParams(info, merchant, type);
        JasperPrint print = JasperFillManager.fillReport(reminder, params, new JREmptyDataSource(1));


        Files.createDirectories(outputPath.getParent());

        JasperExportManager.exportReportToPdfFile(print, outputPath.toString());
        ReportFactory.openPDF(outputPath.toFile());
    }

    private static Map<String, Object> buildParams(ReminderInfo info, ReminderInfo.Merchant merchant, CustomerEnums.ReminderType type) {
        Map<String, Object> params = new HashMap<>();

        params.put("M_NAME", merchant.name());
        params.put("M_ADDRESS", merchant.address());
        params.put("EMAIL", merchant.email());
        if (merchant.logo() != null) {
            params.put("LOGO", new ByteArrayInputStream(merchant.logo()));
        }

        params.put("C_NAME", info.getCustomerName());
        params.put("C_ADDRESS", info.getCustomerAddress());
        params.put("PHONE", info.getCustomerPhone());
        params.put("PREV_MONTH", info.getIssueMonthYear().getMonth());
        params.put("NEW_DUE_DATE", formatDate(info.getNewDueDate()));
        params.put("DEBT", info.getDebt());
        params.put("REM_TYPE", type.name());

        return params;
    }

    private static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }
}
