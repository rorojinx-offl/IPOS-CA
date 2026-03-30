package org.novastack.iposca.utils.db.spawn;

import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class Users {
    static void main() {
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn, "users", initiateTables());
            // ddl.createIndex(conn, "users", "USERNAME");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("ID", "INTEGER", true, true, null));
                add(new TableSchema.Column("USERNAME", "TEXT", false, true, null));
                add(new TableSchema.Column("PASSWORD", "TEXT", false, true, null));
                add(new TableSchema.Column("SALT", "TEXT", false, false, null));
                add(new TableSchema.Column("FULL_NAME", "TEXT", false, true, null));
                add(new TableSchema.Column("ROLE", "TEXT", false, true, null));
                add(new TableSchema.Column("IS_ACTIVE", "INTEGER", false, true, null));
                add(new TableSchema.Column("FAILED_ATTEMPTS", "INTEGER", false, true, null));
                add(new TableSchema.Column("LAST_LOGIN", "TEXT", false, false, null));
                add(new TableSchema.Column("CREATED_AT", "TEXT", false, true, null));
            }
        };
    }
}