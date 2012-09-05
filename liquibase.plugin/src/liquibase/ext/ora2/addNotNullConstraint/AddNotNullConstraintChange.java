package liquibase.ext.ora2.addNotNullConstraint;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.database.Database;
import liquibase.ext.ora2.dropNotNullConstraint.DropNotNullConstraintChange;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.StringUtils;

/**
 * Adds a named not null constraint to an existing column.
 * 
 * @author afinke
 *
 */
public class AddNotNullConstraintChange extends AbstractChange {
    private String schemaName;
    private String tableName;
    private String columnName;
    private String constraintName;
    private String defaultNullValue;


    public AddNotNullConstraintChange() {
        super("addNotNullConstraint", "Add Not-Null Constraint", ChangeMetaData.PRIORITY_DEFAULT + 1);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDefaultNullValue() {
        return defaultNullValue;
    }

    public void setDefaultNullValue(String defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }

    public String getConstraintName() {
		return constraintName;
	}

	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}

    public SqlStatement[] generateStatements(Database database) {

        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        String schemaName = getSchemaName() == null?database.getDefaultSchemaName():getSchemaName();

        if (defaultNullValue != null) {
            String defaultValue = defaultNullValue;

            statements.add(new UpdateStatement(schemaName, getTableName())
                    .addNewColumnValue(getColumnName(), defaultValue)
                    .setWhereClause(getColumnName() + " IS NULL"));
        }

        statements.add(new AddNotNullConstraintStatement(schemaName, getTableName(), getColumnName(), getConstraintName()));

        return statements.toArray(new SqlStatement[statements.size()]);
    }


    @Override
    protected Change[] createInverses() {
        DropNotNullConstraintChange inverse = new DropNotNullConstraintChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setColumnName(getColumnName());
        inverse.setConstraintName(getConstraintName());
        inverse.setDefaultNullValue(getDefaultNullValue());
        inverse.setConstraintName(getConstraintName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Not-Null constraint has been added to " + getTableName() + "." + getColumnName();
    }
}