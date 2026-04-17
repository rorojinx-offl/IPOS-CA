package org.novastack.iposca.utils.db.spawn;

import org.novastack.iposca.utils.db.ColumnConstraint;
import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.TableSchema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class FlexiDiscount {
    static void main(){
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"flexi_dsc", initiateTables());
            ddl.createIndex(conn,"flexi_dsc","CUST_ID");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void create(Connection conn, DDLEngine ddl) throws SQLException {
        ddl.createTable(conn, "flexi_dsc", FlexiDiscount.initiateTables());
        ddl.createIndex(conn,"flexi_dsc","CUST_ID");
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("CUST_ID", "INTEGER", true, true,null));
                add(new TableSchema.Column("RATE", "INTEGER", false, true, new ArrayList<>() {{
                    add(new ColumnConstraint.Default("0"));
                }}));
                add(new TableSchema.ForeignKey("CUST_ID", "customer", "ID", true, true));
            }
        };
    }
}
