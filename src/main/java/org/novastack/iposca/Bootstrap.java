package org.novastack.iposca;

import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.spawn.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

public class Bootstrap {
    public static final Map<String, Path> docsPath = new HashMap<>();

    public static void init() {
        dbInit();
        try {
            docsPathInit();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void dbInit() {
        Connection connection = new SQLiteConnection().getConnection();
        DDLEngine ddl = new DDLEngine();

        try {
            AppConfig.create(connection, ddl);
            Customer.create(connection, ddl);
            CustomerCharge.create(connection, ddl);
            CustomerDebt.create(connection, ddl);
            CustomerMonthlyBalance.create(connection, ddl);
            CustomerMonthlySpend.create(connection, ddl);
            CustomerReminder.create(connection, ddl);
            CustomerRepayment.create(connection, ddl);
            FixedDiscount.create(connection, ddl);
            FlexiDiscount.create(connection, ddl);
            Sale.create(connection, ddl);
            SaleItem.create(connection, ddl);
            Stock.create(connection, ddl);
            User.create(connection, ddl);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void docsPathInit() throws IOException {
        String programData = System.getenv("ProgramData");
        Path base = (programData != null && !programData.isBlank())
                ? Paths.get(programData)
                : Paths.get(System.getProperty("user.home"), ".iposca");

        Path dir = base.resolve("IPOS-CA");
        Path reminders = dir.resolve("reminders");
        Path statements = dir.resolve("statements");
        Path custInvoice = dir.resolve("customer-invoices");
        Path guestInvoice = dir.resolve("guest-invoices");
        Path lowStock = dir.resolve("low-stock-reports");


        Files.createDirectories(dir);
        Files.createDirectories(reminders);
        Files.createDirectories(statements);
        Files.createDirectories(custInvoice);
        Files.createDirectories(guestInvoice);
        Files.createDirectories(lowStock);

        Map<String, Path> docs = Map.ofEntries(
                entry("reminders", reminders),
                entry("statements", statements),
                entry("cinvoice", custInvoice),
                entry("ginvoice", guestInvoice),
                entry("stock", lowStock)
        );

        docsPath.putAll(docs);
    }

    public static Path getDocsPath(String docType) {
        return docsPath.get(docType);
    }
}
