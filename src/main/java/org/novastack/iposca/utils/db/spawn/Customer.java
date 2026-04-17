package org.novastack.iposca.utils.db.spawn;

import org.novastack.iposca.utils.db.ColumnConstraint;
import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class Customer {
    static void main() {
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"customer",initiateTables());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void create(Connection conn, DDLEngine ddl) throws SQLException {
        ddl.createTable(conn, "customer", Customer.initiateTables());
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("ID", "INTEGER", true, false, null));
                add(new TableSchema.Column("NAME", "VARCHAR(255)", false, true, null));
                add(new TableSchema.Column("EMAIL", "VARCHAR(255)", false, true,null));
                add(new TableSchema.Column("ADDRESS", "TEXT", false, true,null));
                add(new TableSchema.Column("PHONE", "VARCHAR(11)", false, true,null));
                add(new TableSchema.Column("CREDITLIMIT", "REAL", false, true,null));
                add(new TableSchema.Column("DSCPLAN", "VARCHAR(255)", false, true,null));
                add(new TableSchema.Column("STATUS", "VARCHAR(255)", false, true,null));
            }
        };
    }
}
