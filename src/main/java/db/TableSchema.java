package db;

import java.util.ArrayList;

public sealed interface TableSchema permits TableSchema.Column, TableSchema.ForeignKey, TableSchema.MultiPrimaryKey {
    record Column(String tupleName, String type, boolean isPrimaryKey, boolean isNotNull, ArrayList<ColumnConstraint> extraConstraints) implements TableSchema {}
    record ForeignKey(String fkColumns, String refTable, String refColumns, boolean onDeleteCascade, boolean onUpdateCascade) implements TableSchema {}
    record MultiPrimaryKey(String pkColumns) implements TableSchema {}
}
