package org.novastack.iposca.utils.db;

import org.sqlite.SQLiteConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

public class SQLiteConnection {
    private static Path resolveDBPath() throws IOException {
        String programData = System.getenv("ProgramData");
        Path base = (programData != null && !programData.isBlank())
                ? Paths.get(programData)
                : Paths.get(System.getProperty("user.home"), ".iposca");

        Path dir = base.resolve("IPOS-CA");
        Files.createDirectories(dir);
        return dir.resolve("iposca.db");
    }

    public Connection getConnection() {
        Connection c = null;

        SQLiteConfig cfg = new SQLiteConfig();
        cfg.enforceForeignKeys(true);

        try {
            Class.forName("org.sqlite.JDBC");
            String jdbcUrl = "jdbc:sqlite:" + resolveDBPath().toAbsolutePath();
            //c = DriverManager.getConnection("jdbc:sqlite:iposca.db", cfg.toProperties());
            c = DriverManager.getConnection(jdbcUrl, cfg.toProperties());
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return c;
    }
}
