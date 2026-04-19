package org.novastack.iposca;

import org.novastack.iposca.cust.debt.DebtAutomationService;
import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.spawn.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Map.entry;

/**
 * Class that handles the initialisation of the application.
 * */
public class Bootstrap {
    public static final Map<String, Path> docsPath = new HashMap<>();
    public static final int TOTAL_STEPS = 17;

    /**
     * Initialises the application by creating the database tables and setting up the docs path.
     * @param progress A {@link BiConsumer} that is used to track the progress of the initialisation.
     * @throws SQLException If an error occurs while connecting to the database.
     * @throws IOException If an error occurs while setting up the docs path.
     * */
    public static void init(BiConsumer<Integer, String> progress) throws SQLException, IOException {
        int step = 0;

        step = dbInit(progress, step);

        progress.accept(++step, "Docs Path Init");
        docsPathInit();
        progress.accept(++step, "Debt Check");
        debtCheck();

        progress.accept(++step, "Starting server and session manager");
    }

    /**
     * Initialises the database by creating the tables.
     * @param progress A {@link BiConsumer} that is used to track the progress of the initialisation.
     * @param step The current step in the initialisation process.
     * @return The updated step.
     * @throws SQLException If an error occurs while connecting to the database.
     * */
    private static int dbInit(BiConsumer<Integer, String> progress, int step) throws SQLException {
        Connection connection = new SQLiteConnection().getConnection();
        DDLEngine ddl = new DDLEngine();

        progress.accept(step++, "Creating tables");
        AppConfig.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        Customer.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        CustomerCharge.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        CustomerDebt.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        CustomerMonthlyBalance.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        CustomerMonthlySpend.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        CustomerReminder.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        CustomerRepayment.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        FixedDiscount.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        FlexiDiscount.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        Sale.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        SaleItem.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        Stock.create(connection, ddl);

        progress.accept(step++, "Creating tables");
        User.create(connection, ddl);

        return step;
    }

    /**
     * Creates the paths for the different types of documents.
     * @throws IOException If an error occurs while creating the directories.
     * */
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
        Path reports = dir.resolve("reports");
        Path debt = reports.resolve("debt");
        Path stock = reports.resolve("stock");
        Path turnover = reports.resolve("turnover");


        Files.createDirectories(dir);
        Files.createDirectories(reminders);
        Files.createDirectories(statements);
        Files.createDirectories(custInvoice);
        Files.createDirectories(guestInvoice);
        Files.createDirectories(lowStock);
        Files.createDirectories(reports);
        Files.createDirectories(debt);
        Files.createDirectories(stock);
        Files.createDirectories(turnover);

        Map<String, Path> docs = Map.ofEntries(
                entry("reminders", reminders),
                entry("statements", statements),
                entry("cinvoice", custInvoice),
                entry("ginvoice", guestInvoice),
                entry("stock", lowStock),
                entry("debt", debt),
                entry("stockReport", stock),
                entry("turnover", turnover)
        );

        docsPath.putAll(docs);
    }

    /**
     * Checks the debt status of all customers and runs the debt automation service if necessary.
     * */
    private static void debtCheck() {
        LocalDate date = LocalDate.now();
        int lastDayOfMonth = date.getMonth().maxLength();
        if (date.isEqual(date.withDayOfMonth(lastDayOfMonth))) DebtAutomationService.runDebtEvaluation();
        DebtAutomationService.runDailyDebtEvaluation();
    }

    public static Path getDocsPath(String docType) {
        return docsPath.get(docType);
    }
}
