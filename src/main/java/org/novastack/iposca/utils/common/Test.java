package org.novastack.iposca.utils.common;

import org.novastack.iposca.utils.db.ColumnConstraint;
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
            ddl.createTable(conn,"customer_reminder",initiateTables());
            ddl.createIndex(conn,"customer_reminder","ID");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("ID", "INTEGER", true, true,null));
                add(new TableSchema.Column("CUST_ID", "INTEGER", false, true,null));
                add(new TableSchema.Column("BILLING_MONTH", "TEXT", false, true,new ArrayList<>() {{
                    add(new ColumnConstraint.Unique());
                }}));
                add(new TableSchema.Column("REMINDER_TYPE", "TEXT", false, true,new ArrayList<>() {{
                    add(new ColumnConstraint.Unique());
                }}));
                add(new TableSchema.Column("GENERATED_DATE", "TEXT", false, true,null));
                add(new TableSchema.ForeignKey("CUST_ID", "customer", "ID", true, true));
            }
        };
    }
}
