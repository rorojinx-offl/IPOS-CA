package org.novastack.iposca.cust;

import org.jooq.exception.DataAccessException;
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
            ddl.createTable(conn,"fixed_dsc",initiateTables());
            ddl.createIndex(conn,"fixed_dsc","CUST_ID");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        ArrayList<TableSchema> list = new ArrayList<>() {
            {
                add(new TableSchema.Column("CUST_ID", "INTEGER", true, false,null));
                add(new TableSchema.Column("RATE", "INTEGER", false, true,new ArrayList<>() {{
                    add(new ColumnConstraint.Unique());
                    add(new ColumnConstraint.Default("20"));
                }}));
                add(new TableSchema.ForeignKey("CUST_ID", "customer", "ID", true, true));
            }
        };

        System.out.println(ddl.createTableStm("fixed_dsc",list));
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("CUST_ID", "INTEGER", true, false,null));
                add(new TableSchema.Column("RATE", "INTEGER", false, true,null));
                add(new TableSchema.ForeignKey("CUST_ID", "customer", "ID", true, true));
            }
        };
    }
}
