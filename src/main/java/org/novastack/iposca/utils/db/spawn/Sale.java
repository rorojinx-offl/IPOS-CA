package org.novastack.iposca.utils.db.spawn;

import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class Sale {
    static void main() {
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"sale",initiateTables());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void create(Connection conn, DDLEngine ddl) throws SQLException {
        ddl.createTable(conn, "sale", Sale.initiateTables());
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("ID", "INTEGER", true, true,null));
                add(new TableSchema.Column("CUST_ID", "INTEGER", false, false,null));
                add(new TableSchema.Column("PAYMENT_METHOD", "TEXT", false, true,null));
                add(new TableSchema.Column("CARD_VENDOR", "TEXT", false, false,null));
                add(new TableSchema.Column("CARD_FIRST_4", "TEXT", false, false,null));
                add(new TableSchema.Column("CARD_LAST_4", "TEXT", false, false,null));
                add(new TableSchema.Column("CARD_EXP", "TEXT", false, false,null));
                add(new TableSchema.Column("SALE_DATE_TIME", "TEXT", false, true,null));
                add(new TableSchema.Column("AMOUNT", "REAL", false, true,null));
                add(new TableSchema.ForeignKey("CUST_ID", "customer", "ID", true, true));
            }
        };
    }
}
