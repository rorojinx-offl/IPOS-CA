package org.novastack.iposca.utils.db.spawn;

import org.novastack.iposca.utils.db.ColumnConstraint;
import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class User {
    static void main() {
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"user",initiateTables());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("ID", "INTEGER", true, true,null));
                add(new TableSchema.Column("USERNAME", "TEXT", false, true,new ArrayList<>() {{
                    add(new ColumnConstraint.Unique());
                }}));
                add(new TableSchema.Column("PASSWORD", "TEXT", false, true,null));
                add(new TableSchema.Column("ROLE", "TEXT", false, true,null));
                add(new TableSchema.Column("FULL_NAME", "TEXT", false, true,null));
                add(new TableSchema.Column("CREATED_AT", "TEXT", false, true,null));
            }
        };
    }
}
