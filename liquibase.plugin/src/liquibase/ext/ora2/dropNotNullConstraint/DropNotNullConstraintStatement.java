package liquibase.ext.ora2.dropNotNullConstraint;

import liquibase.statement.AbstractSqlStatement;

public class DropNotNullConstraintStatement extends AbstractSqlStatement {

    private String schemaName;
    private String tableName;
    private String constraintName;

    public DropNotNullConstraintStatement(String schemaName, String tableName, String constraintName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getConstraintName() {
        return constraintName;
    }

}
