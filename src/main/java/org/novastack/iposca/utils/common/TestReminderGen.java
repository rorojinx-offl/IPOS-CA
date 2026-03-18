package org.novastack.iposca.utils.common;

import org.novastack.iposca.cust.reminders.ReminderFactory;
import org.novastack.iposca.cust.reminders.ReminderInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

public class TestReminderGen {
    static void main() {
        try {
            ReminderInfo info = new ReminderInfo(
                    "Rohit Gurunathan",
                    "116 Rainbow Road, Slade Green, Kent, DA8 2EQ",
                    "1234567890",
                    YearMonth.of(2026, Month.MARCH),
                    LocalDate.now().plusDays(7),
                    2900f
            );
            ReminderInfo.Merchant merchant = new ReminderInfo.Merchant(
                    "T-Pharma",
                    "123 Test Street, Test Town, Testshire, TE1 1ST",
                    "test@tpharma.com",
                    loadLogo());

            Path jrxml = Path.of("/jasper/cust/1stReminder.jrxml");
            Path pdf = Path.of("generated-reports", "debt-reminder1-455.pdf");

            ReminderFactory.generateReminder(info, merchant, pdf, jrxml);
            System.out.println("Reminder generated successfully at: " + pdf.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] loadLogo() throws IOException {
        try (InputStream in = TestReminderGen.class.getResourceAsStream("/ui/cust/assets/debt.png")) {
            if (in == null) {
                throw new IOException("Resource not found");
            }
            return in.readAllBytes();
        }
    }
}
