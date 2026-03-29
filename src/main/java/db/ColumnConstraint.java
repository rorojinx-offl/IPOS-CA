package db;

public sealed interface ColumnConstraint permits ColumnConstraint.Check, ColumnConstraint.Default, ColumnConstraint.Unique {
    record Unique() implements ColumnConstraint {}
    record Default(String value) implements ColumnConstraint {}
    record Check(String condition) implements ColumnConstraint {}
}
