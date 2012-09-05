package liquibase.ext.backupandrestore.restore;

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
 * Generates needed SQL for {@link liquibase.ext.backupandrestore.restore.RestoreChange}.
 * 
 * @author afinke
 *
 */
public class RestoreGenerator extends AbstractSqlGenerator<RestoreStatement> {

	@Override
	public ValidationErrors validate(RestoreStatement restoreStatement,
			Database database, SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tables", restoreStatement.getTablesToRestore());
        return validationErrors;
	}

	@Override
	public Sql[] generateSql(RestoreStatement restoreStatement, Database database,
			SqlGeneratorChain sqlGeneratorChain) {
		
		List<Sql> generatedSql = new ArrayList<Sql>();
		
		for (Table tableToRestore : restoreStatement.getTablesToRestore()) {
			BackupRestore backupRestore = null;
			String sqlDropModifiedTable = null;
			String sqlTableDDL = null;
			String sqlTableData = null;
			List<String> sqlReferentials = null;
			
			// add further implementations if you like to
			if(database instanceof OracleDatabase) {
				backupRestore = new BackupRestoreOracle();
			}
			
			sqlDropModifiedTable = backupRestore.dropModifiedTable(tableToRestore.getSchemaName(), tableToRestore.getTableName(), database);
			if(sqlDropModifiedTable != null) {
				generatedSql.add(new UnparsedSql(sqlDropModifiedTable));	
			}

			sqlTableDDL = backupRestore.restoreTableStructure(tableToRestore.getTableName(), database);
			generatedSql.add(new UnparsedSql(sqlTableDDL));
			
			sqlTableData = backupRestore.restoreTableData(
					tableToRestore.getSchemaName(), 
					tableToRestore.getTableName(), 
					restoreStatement.getPrefix(), 
					database);
			generatedSql.add(new UnparsedSql(sqlTableData));
			
			sqlReferentials = backupRestore.restoreReferentials(tableToRestore.getTableName(), database);
			if((0 < sqlReferentials.size())) {
				for (String sqlReferential : sqlReferentials) {
					generatedSql.add(new UnparsedSql(sqlReferential));
				}
			}
			
			backupRestore.deleteDatabaseRestoreTableLogRecord(tableToRestore.getTableName(), database);
			
			// possible clean up of the backup table
		}
		
		return generatedSql.toArray(new Sql[generatedSql.size()]);
	}

}
