package liquibase.ext.backupandrestore.model;

/**
 * Entity class for tables.
 * 
 * @author afinke
 *
 */
public class Table {

	private String schemaName;
	private String tableName;
	
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
	
}
