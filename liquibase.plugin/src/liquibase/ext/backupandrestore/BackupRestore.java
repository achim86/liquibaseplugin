package liquibase.ext.backupandrestore;

import java.util.List;

import liquibase.database.Database;

/**
 * Helper for {@link liquibase.ext.backupandrestore.backup.BackupGenerator} and
 * {@link liquibase.ext.backupandrestore.restore.RestoreGenerator}. 
 * Provides several methods to help backup and restore tables.
 * 
 * @author afinke
 *
 */
public interface BackupRestore {

	/**
	 * General prefix for backup tables
	 */
	public static final String TABLE_PREFIX = "LIQ_";

	/**
	 * Creates 'DATABASERESTORETABLELOG' table if not existent. This
	 * table is being used to store ddl information about backuped tables.
	 * After creation inserts a record of the given table in possible schema.
	 * 
	 * @param schemaName the table schema, null if default schema
	 * @param tableName the table name
	 * @param database the database
	 */
	public void createDatabaseRestoreTableLogRecord(String schemaName,
												    String tableName, 
													Database database);
	
	/**
	 * Backups the table to backup in a copy named after the given name.
	 * 
	 * @paramschemaName the table schema, null if default schema
	 * @param backupTableName the backup table name
	 * @param database the database
	 * @return the create statement for the backup table
	 */
	public String backupTableData(String schemaName,
								  String backupTableName, 
								  String tableToBackup, 
								  Database database);
	
	/**
	 * Drops the modified table to restore a clean backup.
	 * 
	 * @param schemaName the table schema, null if default schema
	 * @param tableName the table name
	 * @param database the database
	 * @return the drop statement; null if there is no table to drop
	 */
	public String dropModifiedTable(String schemaName, 
								    String tableName, 
								    Database database);
	
	/**
	 * Table DDL of the given table from DATABASERESTORETABLELOG'.
	 * 
	 * @param tableName the table name
	 * @param database the database
	 * @return ddl string of the given table
	 */
	public String restoreTableStructure(String tableName, Database database);
	
	/**
	 * Insert for backuped data.
	 * 
	 * @param schemaName the table schema, null if default schema
	 * @param tableName the table name
	 * @param prefix the prefix of the backup table
	 * @param database the database
	 * @return insert of the data for the given table
	 */
	public String restoreTableData(String schemaName,
								   String tableName, 
								   String prefix,
								   Database database);

	/**
	 * Referential(s) DDL of the given table from DATABASERESTORETABLELOG'.
	 * 
	 * @param tableName the table name
	 * @param database the database
	 * @return ddl referential string of the given table
	 */
	public List<String> restoreReferentials(String tableName, Database database);

	/**
	 * Delete the record for the given table name from 'DATABASERESTORETABLELOG'.
	 * 
	 * @param tableName the table name
	 * @param database the database
	 */
	public void deleteDatabaseRestoreTableLogRecord(String tableName, 
			Database database);
}