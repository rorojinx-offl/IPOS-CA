package org.novastack.iposca.cust;

import org.jooq.exception.DataAccessException;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class Test {
    static void main() {
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"customer",initiateTables());
            ddl.createIndex(conn,"customer","ID");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("ID", "INTEGER", true, false));
                add(new TableSchema.Column("NAME", "VARCHAR(255)", false, true));
                add(new TableSchema.Column("EMAIL", "VARCHAR(255)", false, true));
                add(new TableSchema.Column("ADDRESS", "TEXT", false, true));
                add(new TableSchema.Column("PHONE", "VARCHAR(11)", false, true));
                add(new TableSchema.Column("CREDITLIMIT", "REAL", false, true));
                add(new TableSchema.Column("DSCPLAN", "VARCHAR(255)", false, true));
                add(new TableSchema.Column("STATUS", "VARCHAR(255)", false, true));
            }
        };
    }
}
