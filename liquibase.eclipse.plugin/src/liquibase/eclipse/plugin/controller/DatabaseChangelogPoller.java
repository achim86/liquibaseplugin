package liquibase.eclipse.plugin.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import liquibase.eclipse.plugin.model.ChangeSet;
import liquibase.eclipse.plugin.model.ChangeSetContentProvider;
import liquibase.eclipse.plugin.model.DatabaseConfiguration;
import liquibase.eclipse.plugin.model.Status;


/**
 * Polls the database to get the actual status of liquibase
 * execution from table databasechangelog. Sets ChangeSets 
 * via the ContentProvider to executed if they are listed.
 * 
 * @author afinke
 *
 */
public class DatabaseChangelogPoller implements Runnable {

	private final static int pollInterval = 250;
	
	private boolean initialized;
	private long startTime;
	private volatile boolean isRunning = true;
	private DatabaseConfiguration databaseConfiguration;
	private Connection connection;
	private PreparedStatement preparedStatement;
	private ResultSet resultSet;
	
	public DatabaseChangelogPoller(DatabaseConfiguration databaseConfiguration) {
		this.databaseConfiguration = databaseConfiguration;
	}
	
	@Override
	public void run() {
		// measure start time for the first change set
		startTime = System.currentTimeMillis();
		try {
			Class.forName(LiquibaseViewController.oracleDriver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			connection = DriverManager.getConnection(
					databaseConfiguration.getUrl(), 
					databaseConfiguration.getUser(), 
					databaseConfiguration.getPassword());
			while (isRunning) {
				// poll the databasechangelog table
				pollDatabaseChangeLog();
				try {
					// sleep to take load from database
					Thread.sleep(pollInterval);
				} catch (InterruptedException e) {
					// interrupt is expected
				}
			}
			// final poll to get last run change sets
			pollDatabaseChangeLog();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// not the reason, but a follow up error
			}
		}
	}

	public void stop() {
		isRunning = false;
	}
	
	/**
	 * Polls 'databasechangelog' table to get executed changes at runtime. Needed 
	 * due to the reason that liquibase doesn't except any events to get the actual
	 * status of execution.
	 */
	public void pollDatabaseChangeLog() {
		ChangeSetContentProvider changeSetContentProvider = ChangeSetContentProvider.getInstance();
		if(!initialized) {
			ChangeSet firstChangeSet = changeSetContentProvider.getChangeSets().get(0);
			firstChangeSet.setStatus(Status.RUNNING);
			initialized = isLiqInitialized();
		} else {
			try {
				// order by is need due to the reason that there can be executed many change sets
				// during one poll
				final String sql = "SELECT id FROM databasechangelog ORDER BY orderexecuted";
				preparedStatement = connection.prepareStatement(sql);
				resultSet = preparedStatement.executeQuery();
				while (resultSet.next()) {
					String tmpId = resultSet.getString("id");
					Iterator<ChangeSet> changeSetIterator = 
							changeSetContentProvider.getChangeSets().iterator();
					while (changeSetIterator.hasNext()) {
						ChangeSet changeSet = (ChangeSet) changeSetIterator.next();
						if(changeSet.getId().equalsIgnoreCase(tmpId) && 
						  (changeSet.getStatus().equals(Status.UNRUN) || 
						   changeSet.getStatus().equals(Status.RUNNING))) {
							changeSet.setStatus(Status.EXECUTED);
							// measure time from last execution till now
							long executionTime = System.currentTimeMillis() - startTime;
							changeSet.setExecutionTime(executionTime);
							// start measuring start time for next change set
							startTime = System.currentTimeMillis();
							// set next change set to running
							if(changeSet.getStatus().equals(Status.EXECUTED) && changeSetIterator.hasNext()) {
								changeSet = (ChangeSet) changeSetIterator.next();
								if(changeSet.getStatus().equals(Status.UNRUN)) {
									changeSet.setStatus(Status.RUNNING);
								}
							}
						}
					}
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					resultSet.close();
					preparedStatement.close();
				} catch (SQLException e) {
					// not the reason, but a follow up error
				}
			}
		}
		
	}
	
	// check if 'databasechangelog' table exists (first liquibase run creates this table)
	private boolean isLiqInitialized() {
		try {
			final String sql = "SELECT count(*) FROM user_tables WHERE table_name = 'DATABASECHANGELOG'";
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			int tableExists = 0;
			if (resultSet.next()) {
				tableExists = resultSet.getInt(1);
			}
			if(1 == tableExists) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				resultSet.close();
				preparedStatement.close();
			} catch (SQLException e) {
				// not the reason, but a follow up error
			}
		}	
	}
}
