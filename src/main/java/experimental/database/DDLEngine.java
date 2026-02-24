package experimental.database;

import java.util.ArrayList;
import java.util.Collections;

public class DDLEngine {
    private String identifierCheck(String identifier) {
        if (!identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid identifier: " + identifier);
        }
        return "\"" + identifier + "\"";
    }

    public String createTableStm(String tableName, ArrayList<DatabaseOps.TableSchema> tableSchema) {
       tableName = identifierCheck(tableName);

        StringBuilder stm = new StringBuilder();
        stm.append(String.format("CREATE TABLE IF NOT EXISTS %s (", tableName));

        int i = 1;
        boolean isAtleastOneColumn = false;
        int isFKDeclared = Collections.frequency(tableSchema, DatabaseOps.ForeignKey.class);

        for (DatabaseOps.TableSchema rule : tableSchema) {
            switch (rule) {
                case DatabaseOps.Column col -> {
                    isAtleastOneColumn = true;
                    String tupleName = identifierCheck(col.tupleName());
                    String pk = col.isPrimaryKey() ? "PRIMARY KEY" : "";
                    String notNull = col.isNotNull() ? "NOT NULL" : "";
                    String isEnd = (i < tableSchema.size() && !(tableSchema.get(i + 1) instanceof DatabaseOps.Column)) ? ")" : ", ";

                    stm.append(String.format("%s %s %s %s%s", tupleName, col.type(), pk, notNull, isEnd));
                    i++;
                }
                case DatabaseOps.ForeignKey fk -> {
                    if (!isAtleastOneColumn) {throw new IllegalStateException("Foreign keys can only be added after atleast one column is added");}
                    String refTable = identifierCheck(fk.refTable());
                    String onDeleteCascade = fk.onDeleteCascade() ? "ON DELETE CASCADE" : "";
                    String onUpdateCascade = fk.onUpdateCascade() ? "ON UPDATE CASCADE" : "";

                    stm.append(String.format(", FOREIGN KEY (%s) REFERENCES %s(%s) %s %s", fk.fkColumns(), refTable, fk.refColumns(), onDeleteCascade, onUpdateCascade));
                }
                default -> throw new IllegalStateException("Unexpected value: " + rule);
            }
        }
        return stm.toString();
    }

    public String dropTableStm(String tableName) {
        tableName = identifierCheck(tableName);
        return String.format("DROP TABLE IF EXISTS %s", tableName);
    }

    public String dropIndexStm(String indexName) {
        indexName = identifierCheck(indexName);
        return String.format("DROP INDEX IF EXISTS %s", indexName);
    }

    public String alterTableAddColumnStm(String tableName, String columnName, String columnType) {
        tableName = identifierCheck(tableName);
        columnName = identifierCheck(columnName);
        return String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnType);
    }

    public String createIndexStm(String tableName, String columnName) {
        tableName = identifierCheck(tableName);
        columnName = identifierCheck(columnName);
        return String.format("CREATE INDEX IF NOT EXISTS idx_%s_%s ON %s(%s)", tableName.toLowerCase(), columnName.toLowerCase(), tableName, columnName);
    }
}
