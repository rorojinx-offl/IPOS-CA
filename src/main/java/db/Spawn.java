package db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class Spawn {
    static void main() {
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn, "stock", initiateTables());
            //ddl.createIndex(conn,"customer_reminder","ID");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("ID", "INTEGER", true, true, null));
                add(new TableSchema.Column("NAME", "TEXT", false, true, null));
                add(new TableSchema.Column("COST", "REAL", false, true, null));
                add(new TableSchema.Column("QUANTITY", "INTEGER", false, true, null));
            }
        };
    }
}
