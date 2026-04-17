package org.novastack.iposca.utils.db.spawn;

import org.novastack.iposca.utils.db.ColumnConstraint;
import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class CustomerDebt {
    static void main() {
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"customer_debt",initiateTables());
            ddl.createIndex(conn,"customer_debt","CUST_ID");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void create(Connection conn, DDLEngine ddl) throws SQLException {
        ddl.createTable(conn, "customer_debt", CustomerDebt.initiateTables());
        ddl.createIndex(conn,"customer_debt","CUST_ID");
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("CUST_ID", "INTEGER", true, true, null));
                add(new TableSchema.Column("BALANCE", "REAL", false, false, new ArrayList<>() {{
                    add(new ColumnConstraint.Default("0"));
                }}));
                add(new TableSchema.Column("STATUS_1_REMINDER", "TEXT", false, false, null));
                add(new TableSchema.Column("DATE_1_REMINDER", "TEXT", false, false, null));
                add(new TableSchema.Column("STATUS_2_REMINDER", "TEXT", false, false, null));
                add(new TableSchema.Column("DATE_2_REMINDER", "TEXT", false, false, null));
                add(new TableSchema.Column("STATUS_CHANGED_AT", "TEXT", false, false, null));
                add(new TableSchema.ForeignKey("CUST_ID", "customer", "ID", true, true));
            }
        };
    }
}
