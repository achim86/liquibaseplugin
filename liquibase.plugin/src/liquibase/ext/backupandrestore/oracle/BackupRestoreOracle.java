package liquibase.ext.backupandrestore.oracle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.ext.backupandrestore.BackupRestore;
import liquibase.ext.backupandrestore.model.Referential;

/**
 * Oracle implementation for {@link liquibase.ext.backupandrestore.BackupRestore}
 * 
 * @author afinke
 *
 */
public class BackupRestoreOracle implements BackupRestore {
	
	@Override
	public void createDatabaseRestoreTableLogRecord(String schemaName, 
													String tableName, 
													Database database) {
		
		Connection connection = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		String sql;
		int initialized = 0;
		ResultSet resultSet = null;
		
		try {
			connection = ((JdbcConnection) database.getConnection())
					.getWrappedConnection();
			sql = "SELECT COUNT(*) AS init " +
				  "FROM user_tables " +
				  "WHERE table_name = 'DATABASERESTORETABLELOG'";
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				initialized = resultSet.getInt("init");
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				resultSet.close();
				statement.close();
				// connection is closed by liquibase
			} catch (SQLException e) {
				// not the reason, but a follow up error
			}
		}	
		
		if(initialized == 0) {
			try {
				connection = ((JdbcConnection) database.getConnection())
						.getWrappedConnection();
				sql = "CREATE TABLE databaserestoretablelog( " + 
						  "table_name VARCHAR2(30), " +
						  "backup_date DATE, " + 
						  "table_ddl CLOB NOT NULL, " +
						  "referential_ddl CLOB, " +
						  "CONSTRAINT PK_DATABASERESTORETABLELOG PRIMARY KEY (table_name) " +
					  ")";
				statement = connection.createStatement();
				statement.execute(sql);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					statement.close();
					// connection is closed by liquibase
				} catch (SQLException e) {
					// not the reason, but a follow up error
				}
			}			
		}
		
		try {
			connection = ((JdbcConnection) database.getConnection())
					.getWrappedConnection();
			sql = "INSERT INTO databaserestoretablelog (table_name, backup_date, table_ddl, referential_ddl) " +
				  "VALUES (?, CURRENT_DATE, ?, ?)";
			
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, tableName);
			preparedStatement.setClob(2, backupTableDDL(schemaName, tableName, database));
			String referentials = backupTableReferentialDDL(schemaName, tableName, database);
			if(0 < referentials.length()) {
				preparedStatement.setCharacterStream(3, new StringReader(referentials), referentials.length());
			} else {
				preparedStatement.setNull(3, Types.CLOB);
			}
			preparedStatement.execute();
			connection.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				statement.close();
				// connection is closed by liquibase
			} catch (SQLException e) {
				// not the reason, but a follow up error
			}
		}		
	}
	
	private Clob backupTableDDL(String schemaName, String tableName, Database database) {
		Connection connection = null;
		Statement statement = null;
		StringBuilder sql;
		ResultSet resultSet = null;
		Clob tableDDL = null;
		
		try {
			connection = ((JdbcConnection) database.getConnection())
					.getWrappedConnection();
			sql = new StringBuilder();
			// query dmbs metadata to get ddl of whole objects
			sql.append("SELECT dbms_metadata.get_ddl(")
			// object type table
			.append("'TABLE',")
			// object name is the name of the given table
			.append("'").append(tableName.toUpperCase()).append("'");
			// if there is a schema name add the given schema as 3rd parameter
			if(schemaName != null) {
				sql.append(", '").append(schemaName.toUpperCase()).append("'");
			}
			sql.append(") ddl_table FROM dual");
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql.toString());
			if (resultSet.next()) {
				tableDDL = resultSet.getClob("ddl_table");
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				resultSet.close();
				statement.close();
				// connection is closed by liquibase
			} catch (SQLException e) {
				// not the reason, but a follow up error
			}
		}			
		return tableDDL;
	}
	
	private String backupTableReferentialDDL(String schemaName, String tableName, Database database) {
		Connection connection = null;
		Statement statement = null;
		StringBuilder sql;
		ResultSet resultSet = null;
		List<Referential> referentials = new ArrayList<Referential>();
		StringBuilder sqlReferentials = new StringBuilder();
		
		try {
			connection = ((JdbcConnection) database.getConnection())
					.getWrappedConnection();
			
			sql = new StringBuilder();
			if(schemaName != null) {
				// query all constraints and set owner
				sql.append("SELECT ac.constraint_name, ")
				.append("src.table_name AS src_table_name, ")
				.append("src.column_name AS src_column_name, ")
				.append("ref.table_name AS ref_table_name, ")
				.append("ref.column_name AS ref_column_name ")
				.append("FROM all_constraints ac, all_cons_columns src, all_cons_columns ref ")
				.append("WHERE ac.constraint_name = src.constraint_name ")
				.append("AND ac.r_constraint_name = ref.constraint_name ")
				.append("AND src.position = ref.position ")
				.append("AND ac.constraint_type = 'R' ")
				.append("AND ref.table_name = '").append(tableName.toUpperCase()).append("' ")
				.append("AND ref.owner = '").append(schemaName.toUpperCase()).append("'");
			} else {
				// query user constraints (no owner needed)
				sql.append("SELECT uc.constraint_name, ")
				.append("src.table_name AS src_table_name, ")
				.append("src.column_name AS src_column_name, ")
				.append("ref.table_name AS ref_table_name, ")
				.append("ref.column_name AS ref_column_name ")
				.append("FROM user_constraints uc, user_cons_columns src, user_cons_columns ref ")
				.append("WHERE uc.constraint_name = src.constraint_name ")
				.append("AND uc.r_constraint_name = ref.constraint_name ")
				.append("AND src.position = ref.position ")
				.append("AND uc.constraint_type = 'R' ")
				.append("AND ref.table_name = '").append(tableName.toUpperCase()).append("'");
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql.toString());
			while (resultSet.next()) {
				Referential tmpReferential = new Referential();
				tmpReferential.setConstraintName(resultSet.getString("constraint_name"));
				tmpReferential.setSrcTableName(resultSet.getString("src_table_name"));
				tmpReferential.setSrcColumnName(resultSet.getString("src_column_name"));
				tmpReferential.setRefTableName(resultSet.getString("ref_table_name"));
				tmpReferential.setRefColumnName(resultSet.getString("ref_column_name"));
				referentials.add(tmpReferential);
			}			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				resultSet.close();
				statement.close();
				// connection is closed by liquibase
			} catch (SQLException e) {
				// not the reason, but a follow up error
			}
		}	
		
		// build add foreign key statements with the queried referentials
		if(schemaName != null) {
			for (Referential referential : referentials) {
				sqlReferentials.append("ALTER TABLE \"" + schemaName + "\"." + referential.getSrcTableName() + 
									   " ADD CONSTRAINT " + referential.getConstraintName() + 
									   " FOREIGN KEY (" + referential.getSrcColumnName() + ")" +
									   " REFERENCES \"" + schemaName + "\"." + referential.getRefTableName() + "(" + referential.getRefColumnName() + ");\n");
			}
		}else {
			for (Referential referential : referentials) {
				sqlReferentials.append("ALTER TABLE " + referential.getSrcTableName() + 
									   " ADD CONSTRAINT " + referential.getConstraintName() + 
									   " FOREIGN KEY (" + referential.getSrcColumnName() + ")" +
									   " REFERENCES " + referential.getRefTableName() + "(" + referential.getRefColumnName() + ");\n");
			}
		}
		return sqlReferentials.toString();
	}

	@Override
	public String dropModifiedTable(String schemaName, String tableName, Database database) {
		Connection connection = null;
		Statement statement = null;
		String sql;
		StringBuilder dropSql = new StringBuilder();
		int existing = 0;
		ResultSet resultSet = null;
		
		try {
			connection = ((JdbcConnection) database.getConnection())
					.getWrappedConnection();
			if(schemaName != null) {
				sql = "SELECT COUNT(*) AS init " +
					  "FROM all_tables " +
					  "WHERE table_name = '" + tableName.toUpperCase() + "'" +
					  "AND owner = '" + schemaName.toUpperCase() + "'";
			}else {
				sql = "SELECT COUNT(*) AS init " +
					  "FROM user_tables " +
					  "WHERE table_name = '" + tableName.toUpperCase() + "'";
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				existing = resultSet.getInt("init");
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				resultSet.close();
				statement.close();
				// connection is closed by liquibase
			} catch (SQLException e) {
				// not the reason, but a follow up error
			}
		}	
		
		if(existing == 0) {
			return null;
		}else {
			dropSql.append("DROP TABLE ")
			   .append(database.escapeTableName(schemaName, tableName))
			   .append(" CASCADE CONSTRAINTS");
		}
		return dropSql.toString();
	}
	
	@Override
	public String backupTableData(String schemaName,
							      String backupTableName, 
			  					  String tableToBackup, 
			  					  Database database) {
		StringBuilder backupSql = new StringBuilder();
		backupSql.append("CREATE TABLE ")
		.append(database.escapeTableName(schemaName, backupTableName));
		backupSql.append(" AS SELECT * FROM ")
		.append(database.escapeTableName(schemaName, tableToBackup));
		
		return backupSql.toString();
	}
	
	@Override
	public void deleteDatabaseRestoreTableLogRecord(String tableName,
			Database database) {
		Connection connection = null;
		Statement statement = null;
		StringBuilder sql;
		
		try {
			connection = ((JdbcConnection) database.getConnection())
					.getWrappedConnection();
			sql = new StringBuilder();
			sql.append("DELETE FROM databaserestoretablelog ")
			.append("WHERE table_name = '").append(tableName).append("'");
			statement = connection.createStatement();
			statement.execute(sql.toString());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				statement.close();
				// connection is closed by liquibase
			} catch (SQLException e) {
				// not the reason, but a follow up error
			}
		}			
	}
	
	@Override
	public String restoreTableStructure(String tableName, Database database) {
		Connection connection = null;
		Statement statement = null;
		String sql;
		StringBuilder sqlCreateTable = new StringBuilder();
		ResultSet resultSet = null;
		
		try {
			connection = ((JdbcConnection) database.getConnection())
					.getWrappedConnection();
			sql = "SELECT table_ddl " +
				  "FROM databaserestoretablelog " +
				  "WHERE table_name = '" + tableName + "'";
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql.toString());
			if (resultSet.next()) {
				Clob clobValue = resultSet.getClob("table_ddl");
				// oracle returns a clob so it needs to be converted to string
				BufferedReader reader = new BufferedReader(new InputStreamReader(clobValue.getAsciiStream()));
				String read = null;
				while((read = reader.readLine()) != null ) {
					sqlCreateTable.append(read);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				resultSet.close();
				statement.close();
				// connection is closed by liquibase
			} catch (SQLException e) {
				// not the reason, but a follow up error
			}
		}			
		
		return sqlCreateTable.toString();
	}
	
	@Override
	public String restoreTableData(String schemaName, String tableName, String prefix, Database database) {
		StringBuilder sqlIAS = new StringBuilder();
		sqlIAS.append("INSERT /*+ APPEND */ INTO ")
		.append(database.escapeTableName(schemaName, tableName))
		.append(" SELECT * FROM ");
		StringBuilder backupTablename = new StringBuilder();
		if(schemaName != null) {
			backupTablename.append(schemaName).append(".");
		}
		backupTablename.append(BackupRestore.TABLE_PREFIX);
		if(prefix != null) {
			backupTablename.append(prefix)
			.append("_");
		}
		backupTablename.append(tableName);		
		sqlIAS.append(backupTablename);
			
		return sqlIAS.toString();
	}

	@Override
	public List<String> restoreReferentials(String tableName, Database database) {
		Connection connection = null;
		Statement statement = null;
		String sql;
		StringBuilder sqlCreateReferential = new StringBuilder();
		ResultSet resultSet = null;
		
		try {
			connection = ((JdbcConnection) database.getConnection())
					.getWrappedConnection();
			sql = "SELECT referential_ddl " +
				  "FROM databaserestoretablelog " +
				  "WHERE table_name = '" + tableName + "'";
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql.toString());
			if (resultSet.next()) {
				Clob clobValue = resultSet.getClob("referential_ddl");
				// oracle returns a clob so it needs to be converted to string
				if(clobValue != null) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(clobValue.getAsciiStream()));
					String read = null;
					while((read = reader.readLine()) != null ) {
						sqlCreateReferential.append(read);
					}
				} else {
					return Collections.emptyList();
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				resultSet.close();
				statement.close();
				// connection is closed by liquibase
			} catch (SQLException e) {
				// not the reason, but a follow up error
			}
		}			
		
		String[] referentialArray = sqlCreateReferential.toString().split(";");
		
		return Arrays.asList(referentialArray);
	}

}
