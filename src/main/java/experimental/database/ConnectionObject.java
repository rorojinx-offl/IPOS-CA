package experimental.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionObject {
    public Connection getConnection() {
        Connection c = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return c;
    }
}
