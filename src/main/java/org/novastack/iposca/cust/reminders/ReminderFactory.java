package org.novastack.iposca.cust.reminders;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ReminderFactory {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    public static void generateReminder(ReminderInfo info, ReminderInfo.Merchant merchant, Path outputPath, Path templatePath) {

    }

    private static Map<String, Object> buildParams(ReminderInfo info, ReminderInfo.Merchant merchant) {
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

        return params;
    }

    private static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }
}
