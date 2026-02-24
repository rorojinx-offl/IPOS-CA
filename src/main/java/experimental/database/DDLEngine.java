package experimental.database;

import java.util.ArrayList;

public class DDLEngine {
    private record ChainData(String chain, int length) {}

    private String identifierCheck(String identifier) {
        if (!identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid identifier: " + identifier);
        }
        return "\"" + identifier + "\"";
    }

    private ChainData fkChainIdentifierCheck(String identifier) {
        String[] tokens = identifier.split("\\s*,\\s*");
        int fkChainLength = tokens.length;

        for (int i = 0; i < fkChainLength; i++) {
            String token = tokens[i];
            token = token.replaceAll("\\s","");
            token = identifierCheck(token);
            tokens[i] = token;
        }
        return new ChainData(String.join(", ", tokens), fkChainLength);
    }

    public String createTableStm(String tableName, ArrayList<DatabaseOps.TableSchema> tableSchema) {
       tableName = identifierCheck(tableName);

        StringBuilder stm = new StringBuilder();
        stm.append(String.format("CREATE TABLE IF NOT EXISTS %s (", tableName));

        int i = 0;
        long colObjCount = tableSchema.stream().filter(x -> x instanceof DatabaseOps.Column).count();
        long fkObjCount = tableSchema.stream().filter(x -> x instanceof DatabaseOps.ForeignKey).count();

        for (DatabaseOps.TableSchema rule : tableSchema) {
            switch (rule) {
                case DatabaseOps.Column col -> {
                    if (colObjCount < 1) {throw new IllegalStateException("Atleast one column should be present");}
                    String tupleName = identifierCheck(col.tupleName());
                    String pk = col.isPrimaryKey() ? "PRIMARY KEY" : "";
                    String notNull = col.isNotNull() ? "NOT NULL" : "";
                    String isEnd = ", ";
                    if (i < tableSchema.size() - 1) {
                        if (!(tableSchema.get(i + 1) instanceof DatabaseOps.Column)) {isEnd = ")";}
                    } else if (i == tableSchema.size() - 1) {
                        isEnd = ")";
                    }

                    stm.append(String.format("%s %s %s %s%s", tupleName, col.type(), pk, notNull, isEnd));
                }
                case DatabaseOps.ForeignKey fk -> {
                    ChainData fkColChain = fkChainIdentifierCheck(fk.fkColumns());
                    ChainData refColChain = fkChainIdentifierCheck(fk.refColumns());

                    if (colObjCount < 1) {throw new IllegalStateException("Foreign keys can only be added when atleast one column is present");}
                    if (fkObjCount > 1) {throw new IllegalStateException("Please add multiple foreign key declarations in one statement");}
                    if (fkColChain.length != refColChain.length) {throw new IllegalStateException("Foreign key column count and reference column count should be same");}



                    String refTable = identifierCheck(fk.refTable());
                    String onDeleteCascade = fk.onDeleteCascade() ? "ON DELETE CASCADE" : "";
                    String onUpdateCascade = fk.onUpdateCascade() ? "ON UPDATE CASCADE" : "";

                    stm.append(String.format(", FOREIGN KEY (%s) REFERENCES %s(%s) %s %s", fkColChain.chain, refTable, refColChain.chain, onDeleteCascade, onUpdateCascade));
                }
                default -> throw new IllegalStateException("Unexpected value: " + rule);
            }
            i++;
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