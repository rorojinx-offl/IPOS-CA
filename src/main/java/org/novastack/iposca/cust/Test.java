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
            ddl.createTable(conn,"fixed_dsc",initiateTables());
            ddl.createIndex(conn,"fixed_dsc","CUST_ID");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("CUST_ID", "INTEGER", true, false));
                add(new TableSchema.Column("RATE", "INTEGER", false, true));
                add(new TableSchema.ForeignKey("CUST_ID", "customer", "ID", true, true));
            }
        };
    }
}
