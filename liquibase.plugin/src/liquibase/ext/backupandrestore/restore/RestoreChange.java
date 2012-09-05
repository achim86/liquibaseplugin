package liquibase.ext.backupandrestore.restore;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.CheckSum;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.ext.backupandrestore.backup.BackupChange;
import liquibase.ext.backupandrestore.model.Table;
import liquibase.statement.SqlStatement;

/**
 * Inverse of {@link liquibase.ext.backupandrestore.backup.BackupChange}. 
 * Not listed in schema definition due to the reason that it is just
 * for rollback purposes.
 * 
 * @author afinke
 *
 */
public class RestoreChange extends AbstractChange {

	private String prefix;
	private List<Table> tablesToRestore;
	
	public RestoreChange() {
		super("restore", "Restore", ChangeMetaData.PRIORITY_DEFAULT);
		tablesToRestore = new ArrayList<Table>();
	}

	@Override
	public String getConfirmationMessage() {
		StringBuilder restoredTables = new StringBuilder();
		for (Table table : tablesToRestore) {
			restoredTables.append(table.getTableName());
			restoredTables.append(",");
		}
		restoredTables.delete(restoredTables.length() - 1, restoredTables.length());
		return "Tables " + restoredTables.toString() + " restored";
	}

	@Override
	public SqlStatement[] generateStatements(Database database) {
		
		RestoreStatement restoreStatement = new RestoreStatement(getPrefix());
		
		for (Table tableToRestore : getTablesToRestore()) {
			tableToRestore.setSchemaName(tableToRestore.getSchemaName() == null ? (database == null ? null: database.getDefaultSchemaName()) : tableToRestore.getSchemaName());
			restoreStatement.addTableToRestore(tableToRestore);
		}
		
		List<SqlStatement> statements = new ArrayList<SqlStatement>();
		statements.add(restoreStatement);

		return statements.toArray(new SqlStatement[statements.size()]);
	}

	@Override
    protected Change[] createInverses() {
		BackupChange inverse = new BackupChange();
		
		inverse.setPrefix(getPrefix());
		for (Table table : getTablesToRestore()) {
			inverse.addTable(table);
		}

		return new Change[] { inverse };
    }
	
	@Override
    public boolean supports(Database database) {
		// add further implementations if you like to
        return database instanceof OracleDatabase;
    }
	
	@Override
	public CheckSum generateCheckSum() {
		StringBuilder md5ToBuild = new StringBuilder();
		md5ToBuild.append(getPrefix());
		for (Table table : getTablesToRestore()) {
			md5ToBuild.append(table.getSchemaName())
			.append(".")
			.append(table.getTableName());
		}
		
		return CheckSum.compute(this.getChangeMetaData().getName() + ":" + md5ToBuild.toString());
    }	
	
	public Table createTable() {
		Table table = new Table();
		addTable(table);
		return table;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public List<Table> getTablesToRestore() {
		return tablesToRestore;
	}
	
	public void addTable(Table tableToRestore) {
        tablesToRestore.add(tableToRestore);
    }

}
