package liquibase.ext.backupandrestore.backup;

import java.util.ArrayList;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.ext.backupandrestore.BackupRestore;
import liquibase.ext.backupandrestore.model.Table;
import liquibase.ext.backupandrestore.oracle.BackupRestoreOracle;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;

/**
 * Generates needed SQL for {@link liquibase.ext.backupandrestore.backup.BackupChange}.
 * 
 * @author afinke
 *
 */
public class BackupGenerator extends AbstractSqlGenerator<BackupStatement> {

	@Override
	public ValidationErrors validate(BackupStatement backupStatement,
			Database database, SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tables", backupStatement.getTablesToBackup());
        return validationErrors;
	}

	@Override
	public Sql[] generateSql(BackupStatement backupStatement, Database database,
			SqlGeneratorChain sqlGeneratorChain) {

		BackupRestore backupRestore = null;
		List<Sql> generatedSql = new ArrayList<Sql>();
		
		for (Table tableToBackup : backupStatement.getTablesToBackup()) {
			// add further implementations if you like to
			if(database instanceof OracleDatabase) {
				backupRestore = new BackupRestoreOracle();
			}
			// backup table ddl
			backupRestore.createDatabaseRestoreTableLogRecord(tableToBackup.getSchemaName(), tableToBackup.getTableName(), database);
			
			// backup table dml
			StringBuilder backupTablename = new StringBuilder();
			backupTablename.append(BackupRestore.TABLE_PREFIX);
			if(backupStatement.getPrefix() != null) {
				backupTablename.append(backupStatement.getPrefix())
				.append("_");
			}
			backupTablename.append(tableToBackup.getTableName());
			
			String backupSql = backupRestore.backupTableData(tableToBackup.getSchemaName(),  backupTablename.toString(),  tableToBackup.getTableName(), database);
			
			generatedSql.add(new UnparsedSql(backupSql));
		}
		
		return generatedSql.toArray(new Sql[generatedSql.size()]);
	}

}
