package liquibase.ext.bugfix.loadUpdateData;

import liquibase.statement.core.InsertStatement;

public class FixedInsertOrUpdateStatement extends InsertStatement {
    private String primaryKey;


    public FixedInsertOrUpdateStatement(String schemaName, String tableName, String primaryKey) {
        super(schemaName, tableName);
        this.primaryKey = primaryKey ;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }
}
