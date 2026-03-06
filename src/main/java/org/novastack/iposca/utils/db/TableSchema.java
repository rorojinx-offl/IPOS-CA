package org.novastack.iposca.utils.db;

import java.util.ArrayList;

public sealed interface TableSchema permits TableSchema.Column, TableSchema.ForeignKey {
    record Column(String tupleName, String type, boolean isPrimaryKey, boolean isNotNull, ArrayList<ColumnConstraint> extraConstraints) implements TableSchema {}
    record ForeignKey(String fkColumns, String refTable, String refColumns, boolean onDeleteCascade, boolean onUpdateCascade) implements TableSchema {}
}
