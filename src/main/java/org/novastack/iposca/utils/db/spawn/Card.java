package org.novastack.iposca.utils.db.spawn;

import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class Card {
    static void main() {
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"card",initiateTables());
            //ddl.createIndex(conn,"customer_reminder","ID");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("ID", "INTEGER", true, true,null));
                add(new TableSchema.Column("CUST_ID", "INTEGER", false, true,null));
                add(new TableSchema.Column("VENDOR", "TEXT", false, true,null));
                add(new TableSchema.Column("FIRST_4", "TEXT", false, true,null));
                add(new TableSchema.Column("LAST_4", "TEXT", false, true,null));
                add(new TableSchema.Column("EXPIRY", "TEXT", false, true,null));
                add(new TableSchema.ForeignKey("CUST_ID", "customer", "ID", true, true));
            }
        };
    }
}
