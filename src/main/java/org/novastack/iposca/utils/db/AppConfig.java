package org.novastack.iposca.utils.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class AppConfig {
    static void main(){
        Connection conn = new SQLiteConnection().getConnection();

        DDLEngine ddl = new DDLEngine();
        try {
            ddl.createTable(conn,"app_config", AppConfig.initiateTables());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static ArrayList<TableSchema> initiateTables() {
        return new ArrayList<>() {
            {
                add(new TableSchema.Column("KEY", "TEXT", true, true,new ArrayList<>(){{
                    add(new ColumnConstraint.Unique());
                }}));
                add(new TableSchema.Column("VALUE", "BLOB", false, true,null));
            }
        };
    }
}
