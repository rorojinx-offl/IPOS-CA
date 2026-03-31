package org.novastack.iposca.utils.db;
import org.novastack.iposca.utils.db.ColumnConstraint;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class StockTable {
    static void main(){
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"stock_table", StockTable.initiateTables());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("ITEM_ID", "INTEGER", true, true,null));
                add(new TableSchema.Column("NAME", "TEXT", false, true,null));
                add(new TableSchema.Column("PACKAGE_TYPE", "TEXT", false, true,null));
                add(new TableSchema.Column("UNITS", "TEXT", false, true,null));
                add(new TableSchema.Column("UNITS_IN_A_PACK", "INTEGER", false, true,null));
                add(new TableSchema.Column("BULK_COST", "REAL", false, true,null));
                add(new TableSchema.Column("QUANTITY", "INTEGER", false, false,new ArrayList<>(){{
                    add(new ColumnConstraint.Default("0"));
                }}));
            }
        };
    }
}