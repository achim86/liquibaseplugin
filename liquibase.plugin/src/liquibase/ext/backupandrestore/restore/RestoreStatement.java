package liquibase.ext.backupandrestore.restore;

import java.util.ArrayList;
import java.util.List;

import liquibase.ext.backupandrestore.model.Table;
import liquibase.statement.AbstractSqlStatement;

/**
 * Connector between {@link liquibase.ext.backupandrestore.restore.RestoreChange}
 * and {@link liquibase.ext.backupandrestore.restore.RestoreGenerator} which
 * delivers the information provided by the change set.
 * 
 * @author afinke
 *
 */
public class RestoreStatement extends AbstractSqlStatement {

	private String prefix;
	private List<Table> tablesToRestore = new ArrayList<Table>();
	
	public RestoreStatement(String prefix) {
		this.prefix = prefix;
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
	
	public void addTableToRestore(Table tableToRestore) {
		tablesToRestore.add(tableToRestore);
	}

}
