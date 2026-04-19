package org.novastack.iposca.cust.reminders;

import net.sf.jasperreports.engine.*;
import org.novastack.iposca.PDF;
import org.novastack.iposca.cust.customer.CustomerEnums;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory class that generates PDF reminders for customer debt using JasperReports.
 * */
public class ReminderFactory {
    /** Format for displaying dates in the PDF.*/
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    /**
     * Generates a PDF reminder for a customer debt. With the given parameters, it loads the JasperReport schema and
     * compiles it, then fills it with the built parameters, exports it to a PDF file, and opens it.
     * @param info The information about the customer debt.
     * @param merchant The merchant information.
     * @param type The type of reminder to generate (first or second).
     * @param outputPath The path to the output PDF file.
     * @param templatePath The path to the JasperReport schema.
     * @throws JRException If there is an error in the JasperReport compilation.
     * */
    public static void generateReminder(ReminderInfo info, ReminderInfo.Merchant merchant, CustomerEnums.ReminderType type, Path outputPath, Path templatePath) throws JRException, IOException {
        InputStream jrxml = ReminderFactory.class.getResourceAsStream(templatePath.toString());
        if (jrxml == null) {
            throw new IOException("Resource not found: " + templatePath);
        }

        JasperReport reminder = JasperCompileManager.compileReport(jrxml);
        Map<String, Object> params = buildParams(info, merchant, type);
        JasperPrint print = JasperFillManager.fillReport(reminder, params, new JREmptyDataSource(1));

        JasperExportManager.exportReportToPdfFile(print, outputPath.toString());
        PDF.openPDF(outputPath.toFile());
    }

    /**
     * Builds JasperReport parameters with the data provided by {@link #generateReminder(ReminderInfo, ReminderInfo.Merchant, CustomerEnums.ReminderType, Path, Path)}
     * and maps the parameters ({@link String}) to a generic {@link Object} to allow for any datatype (however, the
     * JasperReports compiler will complain if the datatype does not match the parameter type in the schema).
     * @param info The information about the customer debt.
     * @param merchant The merchant information.
     * @param type The type of reminder to generate (first or second).
     * @return A {@link Map} of parameter names ({@link String}) to their respective values ({@link Object}).
     * */
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
