package experimental.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseOps {
    public sealed interface TableSchema permits Column, ForeignKey{}
    public record Column(String tupleName, String type, boolean isPrimaryKey, boolean isNotNull) implements TableSchema {}
    public record ForeignKey(String fkColumns, String refTable, String refColumns, boolean onDeleteCascade, boolean onUpdateCascade) implements TableSchema {}
    private static DDLEngine DDLEngine;
    private static Statement stm;

    static void main() {
        ConnectionObject c = new ConnectionObject();
        Connection connection = c.getConnection();
        System.out.println("Connection established");

        //Create a table
        /*ArrayList<Column> tableSchema = new ArrayList<>(){
            {
                add(new Column("ID", "INTEGER", true, false));
                add(new Column("NAME", "TEXT", false, true));
                add(new Column("AGE", "INT", false, true));
                add(new Column("ADDRESS", "CHAR(100)", false, false));
                add(new Column("SALARY", "REAL", false, false));
            }
        };*/

        ArrayList<TableSchema> tableSchema = new ArrayList<>() {
            {
                add(new Column("ID", "INTEGER", true, false));
                add(new Column("NAME", "TEXT", false, true));
                add(new Column("AGE", "INT", false, true));
                add(new Column("ADDRESS", "CHAR(100)", false, false));
                add(new Column("SALARY", "REAL", false, false));
            }
        };

        DDLEngine = new DDLEngine();
        //createTable(connection, "COMPANY", tableSchema);
        System.out.println(DDLEngine.createTableStm("COMPANY",tableSchema));

        //insertData(connection);

        //queryData(connection);
        closeConnection(connection);
    }

    private static void createTable(Connection connection, String tableName, ArrayList<TableSchema> tableSchema) {
        try {
            stm = connection.createStatement();
            String sql = DDLEngine.createTableStm(tableName,tableSchema);
            stm.executeUpdate(sql);
            stm.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static void insertData(Connection connection) {
        try {
            stm = connection.createStatement();
            String sql = "INSERT INTO COMPANY (NAME, AGE, ADDRESS, SALARY) VALUES ('Carol',24,'New York',50000)";
            stm.executeUpdate(sql);
            stm.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static void queryData(Connection connection) {
        try {
            stm = connection.createStatement();
            ResultSet rs;
            String sql = "SELECT * FROM COMPANY";
            rs = stm.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getString("NAME") + " " + rs.getString("AGE") + " " + rs.getString("ADDRESS") + " " + rs.getString("SALARY"));
            }
            stm.close();
        } catch (Exception e) {System.err.println(e.getClass().getName() + ": " + e.getMessage());}
    }

    private static void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (Exception e) {System.err.println(e.getClass().getName() + ": " + e.getMessage());}
    }
}
