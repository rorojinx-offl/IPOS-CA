package org.novastack.iposca.utils.db.spawn;

import org.novastack.iposca.utils.db.ColumnConstraint;
import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class CustomerRepayment {
    static void main() {
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"customer_repayment",initiateTables());
            ddl.createIndex(conn,"customer_repayment","ID");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void create(Connection conn, DDLEngine ddl) throws SQLException {
        ddl.createTable(conn, "customer_repayment", CustomerRepayment.initiateTables());
        ddl.createIndex(conn,"customer_repayment","ID");
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("ID", "INTEGER", true, true,null));
                add(new TableSchema.Column("CUST_ID", "INTEGER", false, true,null));
                add(new TableSchema.Column("AMOUNT", "REAL", false, true,new ArrayList<>() {{
                    add(new ColumnConstraint.Default("0"));
                }}));
                add(new TableSchema.Column("DATE", "TEXT", false, true,null));
                add(new TableSchema.Column("METHOD", "TEXT", false, true,null));
                add(new TableSchema.Column("CARD_TYPE", "TEXT", false, false,null));
                add(new TableSchema.Column("CARD_FIRST4", "TEXT", false, false,null));
                add(new TableSchema.Column("CARD_LAST4", "TEXT", false, false,null));
                add(new TableSchema.Column("CARD_EXP", "TEXT", false, false,null));
                add(new TableSchema.ForeignKey("CUST_ID", "customer", "ID", true, true));
            }
        };
    }
}
