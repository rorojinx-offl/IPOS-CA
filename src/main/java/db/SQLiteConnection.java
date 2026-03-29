package db;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLiteConnection {
    public Connection getConnection() {
        Connection c = null;

        SQLiteConfig cfg = new SQLiteConfig();
        cfg.enforceForeignKeys(true);

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:http.db", cfg.toProperties());
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return c;
    }
}
