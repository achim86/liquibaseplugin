package liquibase.ext.ora2.addNotNullConstraint;

import liquibase.statement.AbstractSqlStatement;

public class AddNotNullConstraintStatement extends AbstractSqlStatement {
    private String schemaName;
    private String tableName;
    private String columnName;
    private String constraintName;

    public AddNotNullConstraintStatement(String schemaName, String tableName, String columnName, String constraintName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.constraintName = constraintName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

	public String getConstraintName() {
		return constraintName;
	}

}
