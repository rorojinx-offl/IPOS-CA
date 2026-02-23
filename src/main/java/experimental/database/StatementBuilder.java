package experimental.database;

import java.util.ArrayList;
import java.util.StringJoiner;

public class StatementBuilder {
    public String createTableStm(String tableName, ArrayList<DatabaseOps.Table> tableSchema) {
        /*String stm = "";
        stm = stm.concat(String.format("CREATE TABLE IF NOT EXISTS %s (", tableName));*/
        StringBuilder stm = new StringBuilder();
        stm.append(String.format("CREATE TABLE IF NOT EXISTS %s (", tableName));

        int i = 1;
        for (DatabaseOps.Table tuple : tableSchema) {
            String pk = tuple.isPrimaryKey() ? "PRIMARY KEY" : "";
            String notNull = tuple.isNotNull() ? "NOT NULL" : "";
            String isEnd = i == tableSchema.size() ? ")" : ", ";

            stm.append(String.format("%s %s %s %s%s", tuple.tupleName(), tuple.type(), pk, notNull, isEnd));
            i++;
        }
        return stm.toString();
    }
}
