package liquibase.ext.ora2.dropNotNullConstraint;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.database.Database;
import liquibase.ext.ora2.addNotNullConstraint.AddNotNullConstraintChange;
import liquibase.statement.SqlStatement;

/**
 * Drops not null constraint.
 * 
 * @author afinke
 *
 */
public class DropNotNullConstraintChange extends AbstractChange {

	private String schemaName;
	private String tableName;
	private String columnName;
	private String constraintName;
	private String defaultNullValue;

	public DropNotNullConstraintChange() {
		super("dropNotNullConstraint", "Drop Not Null Constraint", ChangeMetaData.PRIORITY_DEFAULT + 1);
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
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

	public String getConstraintName() {
		return constraintName;
	}

	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}

	public String getDefaultNullValue() {
		return defaultNullValue;
	}

	public void setDefaultNullValue(String defaultNullValue) {
		this.defaultNullValue = defaultNullValue;
	}

	public SqlStatement[] generateStatements(Database database) {

		String schemaName = getSchemaName() == null ? database
				.getDefaultSchemaName() : getSchemaName();

		DropNotNullConstraintStatement statement = new DropNotNullConstraintStatement(
				schemaName, getTableName(), getConstraintName());

		return new SqlStatement[] { statement };
	}

	@Override
	protected Change[] createInverses() {
		AddNotNullConstraintChange inverse = new AddNotNullConstraintChange();
		inverse.setSchemaName(getSchemaName());
		inverse.setTableName(getTableName());
		inverse.setConstraintName(getConstraintName());

		return new Change[] { inverse, };
	}

	public String getConfirmationMessage() {
		return "Not-Null constraint removed from " + getTableName();
	}
}
