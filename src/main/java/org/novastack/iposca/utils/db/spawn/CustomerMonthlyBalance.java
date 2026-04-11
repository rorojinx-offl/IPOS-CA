package org.novastack.iposca.utils.db.spawn;

import org.novastack.iposca.utils.db.ColumnConstraint;
import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class CustomerMonthlyBalance {
    static void main() {
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"customer_monthly_balance",initiateTables());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void create(Connection conn, DDLEngine ddl) throws SQLException {
        ddl.createTable(conn, "customer_monthly_balance", CustomerMonthlyBalance.initiateTables());
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("CUST_ID", "INTEGER", false, true,new ArrayList<>() {{
                    add(new ColumnConstraint.Unique());
                }}));
                add(new TableSchema.Column("MONTH_YEAR", "TEXT", false, true,new ArrayList<>() {{
                    add(new ColumnConstraint.Unique());
                }}));
                add(new TableSchema.Column("BALANCE_DUE", "REAL", false, true,null));
                add(new TableSchema.ForeignKey("CUST_ID", "customer", "ID", true, true));
            }
        };
    }
}
