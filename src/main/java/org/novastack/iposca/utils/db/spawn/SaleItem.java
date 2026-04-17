package org.novastack.iposca.utils.db.spawn;

import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class SaleItem {
    static void main() {
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"sale_item",initiateTables());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void create(Connection conn, DDLEngine ddl) throws SQLException {
        ddl.createTable(conn, "sale_item", SaleItem.initiateTables());
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("ID", "INTEGER", true, true,null));
                add(new TableSchema.Column("SALE_ID", "INTEGER", false, false,null));
                add(new TableSchema.Column("PRODUCT_ID", "INTEGER", false, false,null));
                add(new TableSchema.Column("QUANTITY", "INTEGER", false, true,null));
                add(new TableSchema.Column("PRICE", "REAL", false, true,null));
                add(new TableSchema.Column("SUBTOTAL", "REAL", false, true,null));
                add(new TableSchema.ForeignKey("SALE_ID", "sale", "ID", true, true));
                add(new TableSchema.ForeignKey("PRODUCT_ID", "stock", "ITEM_ID", true, true));
            }
        };
    }
}
