package org.novastack.iposca.utils.db;

public sealed interface TableSchema permits TableSchema.Column, TableSchema.ForeignKey {
    record Column(String tupleName, String type, boolean isPrimaryKey, boolean isNotNull) implements TableSchema {}
    record ForeignKey(String fkColumns, String refTable, String refColumns, boolean onDeleteCascade, boolean onUpdateCascade) implements TableSchema {}
}
