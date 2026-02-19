package experimental.database;

import java.util.ArrayList;

public class StatementBuilder {
    public String createTableStm(String tableName, ArrayList<DatabaseOps.Table> tableSchema) {
        String stm = "";
        stm = stm.concat(String.format("CREATE TABLE IF NOT EXISTS %s (", tableName));

        for (DatabaseOps.Table tuple : tableSchema) {
            String pk = tuple.isPrimaryKey() ? "PRIMARY KEY" : "";
            String notNull = tuple.isNotNull() ? "NOT NULL" : "";
            String isEnd = tableSchema.indexOf(tuple) == tableSchema.size() - 1 ? ")" : ", ";

            stm = stm.concat(String.format("%s %s %s %s%s", tuple.tupleName(), tuple.type(), pk, notNull, isEnd));
        }
        return stm;
    }
}
