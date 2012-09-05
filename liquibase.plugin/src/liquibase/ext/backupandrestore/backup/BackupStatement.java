package liquibase.ext.backupandrestore.backup;

import java.util.ArrayList;
import java.util.List;

import liquibase.ext.backupandrestore.model.Table;
import liquibase.statement.AbstractSqlStatement;

/**
 * Connector between {@link liquibase.ext.backupandrestore.backup.BackupChange}
 * and {@link liquibase.ext.backupandrestore.backup.BackupGenerator} which
 * delivers the information provided by the change set.
 * 
 * @author afinke
 *
 */
public class BackupStatement extends AbstractSqlStatement {

	private String prefix;
	private List<Table> tablesToBackup = new ArrayList<Table>();
	
	public BackupStatement(String prefix) {
		this.prefix = prefix;
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
	
	public void addTableToBackup(Table tableToBackup) {
		tablesToBackup.add(tableToBackup);
	}
	
}
