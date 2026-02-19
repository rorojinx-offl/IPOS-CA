package experimental.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseOps {
    public record Table(String tupleName, String type, boolean isPrimaryKey, boolean isNotNull) {}
    private static StatementBuilder statementBuilder;
    private static Statement stm;

    static void main() {
        ConnectionObject c = new ConnectionObject();
        Connection connection = c.getConnection();
        System.out.println("Connection established");

        //Create a table
        ArrayList<Table> tableSchema = new ArrayList<>(){
            {
                add(new Table("ID", "INTEGER", true, false));
                add(new Table("NAME", "TEXT", false, true));
                add(new Table("AGE", "INT", false, true));
                add(new Table("ADDRESS", "CHAR(100)", false, false));
                add(new Table("SALARY", "REAL", false, false));
            }
        };

        statementBuilder = new StatementBuilder();
        createTable(connection, "COMPANY", tableSchema);
        System.out.println(statementBuilder.createTableStm("COMPANY",tableSchema));

        //insertData(connection);

        queryData(connection);
    }

    private static void createTable(Connection connection, String tableName, ArrayList<Table> tableSchema) {
        try {
            stm = connection.createStatement();
            String sql = statementBuilder.createTableStm(tableName,tableSchema);
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
}
