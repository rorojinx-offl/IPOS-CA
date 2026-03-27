package org.novastack.iposca.templates.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:iposca.db";

    /**
     * Get a connection to the SQLite database
     */
    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(DB_URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }

    /**
     * Initialize all database tables for the Templates feature
     * Call this once when the application starts
     */
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // 1. Templates table
            String createTemplatesTable = """
                CREATE TABLE IF NOT EXISTS templates (
                    template_type TEXT PRIMARY KEY,
                    header_text TEXT,
                    body_text TEXT NOT NULL,
                    footer_text TEXT,
                    show_logo INTEGER DEFAULT 0,
                    last_updated_by TEXT NOT NULL,
                    last_updated_at DATETIME NOT NULL
                )
            """;
            stmt.execute(createTemplatesTable);

            // 2. Shop Identity table
            String createShopIdentityTable = """
                CREATE TABLE IF NOT EXISTS shop_identity (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    pharmacy_name TEXT NOT NULL,
                    address TEXT NOT NULL,
                    email TEXT NOT NULL,
                    logo_path TEXT,
                    last_updated_by TEXT NOT NULL,
                    last_updated_at DATETIME NOT NULL
                )
            """;
            stmt.execute(createShopIdentityTable);

            // 3. Audit Log table
            String createAuditLogTable = """
                CREATE TABLE IF NOT EXISTS audit_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    action TEXT NOT NULL,
                    details TEXT,
                    user TEXT NOT NULL,
                    timestamp DATETIME NOT NULL
                )
            """;
            stmt.execute(createAuditLogTable);

            // Insert default data
            insertDefaultTemplates(conn);
            insertDefaultShopIdentity(conn);

        } catch (SQLException e) {
            throw new SQLException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    /**
     * Insert default templates if they don't exist
     */
    private static void insertDefaultTemplates(Connection conn) throws SQLException {
        // Receipt template
        String insertReceipt = """
            INSERT OR IGNORE INTO templates 
            (template_type, header_text, body_text, footer_text, show_logo, last_updated_by, last_updated_at)
            VALUES ('RECEIPT', 'INVOICE', 
                    '{PHARMACY_NAME}\\n{PHARMACY_ADDRESS}\\n{PHARMACY_EMAIL}\\n\\n' ||
                    '--- ITEMS ---\\n\\n' ||
                    'Thank you for your purchase!', 
                    '', 1, 'system', datetime('now'))
        """;

        // Reminder 1 template (first reminder)
        String insertReminder1 = """
            INSERT OR IGNORE INTO templates 
            (template_type, header_text, body_text, footer_text, show_logo, last_updated_by, last_updated_at)
            VALUES ('REMINDER_1', NULL, 
                    'Dear {CUSTOMER_NAME},\\n\\n' ||
                    'This is a reminder that your outstanding balance of {BALANCE} is due.\\n' ||
                    'Please make payment by the end of the month to avoid account suspension.\\n\\n' ||
                    'Thank you,\\n{PHARMACY_NAME}\\n{PHARMACY_ADDRESS}\\n{PHARMACY_EMAIL}', 
                    'Payment Reminder', 0, 'system', datetime('now'))
        """;

        // Reminder 2 template (final reminder)
        String insertReminder2 = """
            INSERT OR IGNORE INTO templates 
            (template_type, header_text, body_text, footer_text, show_logo, last_updated_by, last_updated_at)
            VALUES ('REMINDER_2', NULL, 
                    'Dear {CUSTOMER_NAME},\\n\\n' ||
                    'FINAL NOTICE: Your outstanding balance of {BALANCE} is now overdue.\\n' ||
                    'Your account has been SUSPENDED. No further credit purchases are allowed.\\n' ||
                    'Please contact us immediately to resolve this matter.\\n\\n' ||
                    '{PHARMACY_NAME}\\n{PHARMACY_ADDRESS}\\n{PHARMACY_EMAIL}', 
                    'URGENT: Account Suspended', 0, 'system', datetime('now'))
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(insertReceipt);
            stmt.execute(insertReminder1);
            stmt.execute(insertReminder2);
        }
    }

    /**
     * Insert default shop identity if it doesn't exist
     */
    private static void insertDefaultShopIdentity(Connection conn) throws SQLException {
        String insertIdentity = """
            INSERT OR IGNORE INTO shop_identity 
            (id, pharmacy_name, address, email, logo_path, last_updated_by, last_updated_at)
            VALUES (1, 'HealthPlus Pharmacy', 
                    '123 Main Street, London, UK', 
                    'info@healthplus.com', 
                    '', 'system', datetime('now'))
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(insertIdentity);
        }
    }

    /**
     * Log an audit event
     */
    public static void logAudit(String action, String details, String user) throws SQLException {
        String sql = "INSERT INTO audit_log (action, details, user, timestamp) VALUES (?, ?, ?, datetime('now'))";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setString(2, details);
            ps.setString(3, user);
            ps.executeUpdate();
        }
    }

    /**
     * Load a template by type
     */
    public static java.sql.ResultSet loadTemplate(String type) throws SQLException {
        String sql = "SELECT template_type, header_text, body_text, footer_text, show_logo, last_updated_by, last_updated_at " +
                "FROM templates WHERE template_type = ?";

        Connection conn = connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, type);
        return ps.executeQuery();
        // Note: Caller must close the connection
    }

    /**
     * Save or update a template
     */
    public static void saveTemplate(String type, String header, String body, String footer,
                                    boolean showLogo, String user) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO templates 
            (template_type, header_text, body_text, footer_text, show_logo, last_updated_by, last_updated_at)
            VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
        """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, header);
            ps.setString(3, body);
            ps.setString(4, footer);
            ps.setInt(5, showLogo ? 1 : 0);
            ps.setString(6, user);
            ps.executeUpdate();
        }
    }

    /**
     * Load shop identity
     */
    public static java.sql.ResultSet loadShopIdentity() throws SQLException {
        String sql = "SELECT id, pharmacy_name, address, email, logo_path, last_updated_by, last_updated_at " +
                "FROM shop_identity WHERE id = 1";

        Connection conn = connect();
        PreparedStatement ps = conn.prepareStatement(sql);
        return ps.executeQuery();
        // Note: Caller must close the connection
    }

    /**
     * Save or update shop identity
     */
    public static void saveShopIdentity(String pharmacyName, String address, String email,
                                        String logoPath, String user) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO shop_identity 
            (id, pharmacy_name, address, email, logo_path, last_updated_by, last_updated_at)
            VALUES (1, ?, ?, ?, ?, ?, datetime('now'))
        """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pharmacyName);
            ps.setString(2, address);
            ps.setString(3, email);
            ps.setString(4, logoPath);
            ps.setString(5, user);
            ps.executeUpdate();
        }
    }
}