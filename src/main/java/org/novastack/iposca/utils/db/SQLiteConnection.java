package org.novastack.iposca.utils.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLiteConnection {
    public Connection getConnection() {
        Connection c = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:iposca.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return c;
    }
}
