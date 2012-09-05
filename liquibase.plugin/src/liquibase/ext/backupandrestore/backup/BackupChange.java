package liquibase.ext.backupandrestore.backup;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.CheckSum;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.ext.backupandrestore.model.Table;
import liquibase.ext.backupandrestore.restore.RestoreChange;
import liquibase.statement.SqlStatement;

/**
 * Provides <backup>-Tag for liquibase change sets. Which takes a backup 
 * of table structure and data. Supports the Liquibase rollback with its
 * inverse {@link liquibase.ext.backupandrestore.restore.RestoreChange}.
 * 
 * @author afinke
 *
 */
public class BackupChange extends AbstractChange {

	private String prefix;
	private List<Table> tablesToBackup;
	
	public BackupChange() {
		super("backup", "Backup", ChangeMetaData.PRIORITY_DEFAULT);
		tablesToBackup = new ArrayList<Table>();
	}

	@Override
	public String getConfirmationMessage() {
		StringBuilder backupedTables = new StringBuilder();
		for (Table table : tablesToBackup) {
			backupedTables.append(table.getTableName());
			backupedTables.append(",");
		}
		backupedTables.delete(backupedTables.length() - 1, backupedTables.length());
		return "Tables " + backupedTables.toString() + " backuped";
	}

	@Override
	public SqlStatement[] generateStatements(Database database) {
			
		BackupStatement backupStatement = new BackupStatement(getPrefix());
		
		for (Table tableToBackup : getTablesToBackup()) {
			tableToBackup.setSchemaName(tableToBackup.getSchemaName() == null ? (database == null ? null: database.getDefaultSchemaName()) : tableToBackup.getSchemaName());
			backupStatement.addTableToBackup(tableToBackup);
		}
		
		List<SqlStatement> statements = new ArrayList<SqlStatement>();
		statements.add(backupStatement);

		return statements.toArray(new SqlStatement[statements.size()]);
	}

	@Override
    public Change[] createInverses() {
		RestoreChange inverse = new RestoreChange();
		
		inverse.setPrefix(getPrefix());
		for (Table table : getTablesToBackup()) {
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
		for (Table table : getTablesToBackup()) {
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

	public List<Table> getTablesToBackup() {
		return tablesToBackup;
	}
	
	public void addTable(Table tableToBackup) {
        tablesToBackup.add(tableToBackup);
    }
	
}
