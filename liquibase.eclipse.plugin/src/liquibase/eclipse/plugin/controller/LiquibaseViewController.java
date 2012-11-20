package liquibase.eclipse.plugin.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import liquibase.Liquibase;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.eclipse.plugin.Activator;
import liquibase.eclipse.plugin.model.ChangeSet;
import liquibase.eclipse.plugin.model.ChangeSetStatus;
import liquibase.eclipse.plugin.model.DatabaseConfiguration;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;


/**
 * Controller for {@link liquibase.eclipse.plugin.view.views.LiquibaseView}.
 * 
 * @author afinke
 *
 */
public class LiquibaseViewController {
	public static final String BASE_FOLDER = "database";
	public static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
	
	private Connection connection = null;
	private Database database;
	private DatabaseConfiguration databaseConfiguration;
	private String changeLogPath;
	private Liquibase liquibase;
	private Button releaseButton;

	public LiquibaseViewController() { }
	
	/**
	 * Handles the connection to the database. Use this connection for custom queries.
	 * 
	 * @return the connection
	 * @throws SQLException 
	 */
	public Connection getConnectionInstance() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = DriverManager.getConnection(
					databaseConfiguration.getUrl(), 
					databaseConfiguration.getUser(), 
					databaseConfiguration.getPassword());
		}
		return connection;
	}
	
	/**
	 * Initializes Liquibase with the given change log.
	 * 
	 * @param changeLogPath the change log path
	 * @param databaseConfiguration the database configuration
	 * @param displayAllChangeSets true if already run change sets should be displayed 
	 * 		  	                   false if only unrun change sets should be displayed
	 * @throws LiquibaseException
	 * @throws SQLException 
	 */
	public void initializeChangeLog(String changeLogPath, DatabaseConfiguration databaseConfiguration, boolean displayAllChangeSets) throws LiquibaseException, SQLException {
		this.changeLogPath = changeLogPath;
		this.databaseConfiguration = databaseConfiguration;
		try {
			Class.forName(ORACLE_DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		cleanChangeSets();
		initializeLiquibase(true);
		initializeChangeSets(liquibase, displayAllChangeSets);

	}
	
	/**
	 * Starts {@link liquibase.eclipse.plugin.controller.RelaseJob}. 
	 * 
	 * @throws SQLException 
	 * @throws LiquibaseException 
	 */
	public void release(Shell shell) throws SQLException, LiquibaseException {
		initializeLiquibase(false);
		ReleaseJob releaseJob = new ReleaseJob("Liquibase Release", getConnectionInstance(), liquibase, releaseButton, shell);
		releaseJob.schedule();
	}
	
	/**
	 * Returns the last version to rollback to.
	 * 
	 * @return the version to rollback to
	 */
	public String getLastVersion() {
		Statement statement = null;
		ResultSet resultSet = null;
		Set<String> existingTagsSet = null;
		try {
			String sql = "SELECT tag, orderexecuted " + 
						 "FROM databasechangelog " +
						 "WHERE tag IS NOT NULL " +
						 "ORDER BY orderexecuted";
			statement = getConnectionInstance().createStatement();
			resultSet = statement.executeQuery(sql.toString());
			/* The query may return a tag multiple times; so its uniqueness
			   will be guaranteed by using a set. */
			existingTagsSet = new LinkedHashSet<String>();
			while (resultSet.next()) {
				String tmpTag = resultSet.getString("tag");
				System.out.println(tmpTag);
				existingTagsSet.add(tmpTag);
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
		// Due to the reason a set is difficult to access, its castet to a list.
		List<String> existingTagList = new ArrayList<String>(existingTagsSet);
		for (String string : existingTagList) {
			System.out.println(string);
		}
		switch(existingTagList.size()) {
			case 0: return null;
			case 1: return existingTagList.get(0);
			default: return existingTagList.get(existingTagList.size() - 2);
		}
	}
	
	/**
	 * Starts Liquibase rollback to the given version.
	 * 
	 * @param version the version
	 * @throws LiquibaseException 
	 * @throws SQLException 
	 */
	public void restore(String version) throws SQLException, LiquibaseException {
		initializeLiquibase(false);
		try {
			liquibase.rollback(version, null);
		} catch (LiquibaseException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Initializes the drop down list with saved Liquibase configurations.
	 * 
	 * @param liqConfDropDown the drop down list
	 */
	public void initializeliqConfDropDown(Combo liqConfDropDown) {
		List<String> entries = new ArrayList<String>();
		liqConfDropDown.removeAll();
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		if (preferences.nodeExists(Activator.PLUGIN_ID)) {
			ISecurePreferences node = preferences.node(Activator.PLUGIN_ID);
			for (String key : node.keys()) {
				try {
					if (key.startsWith("name:")) {
						entries.add(node.get(key, "n/a"));
					}
				} catch (StorageException e) {
					e.printStackTrace();
				}
			}
		}
		Collections.sort(entries);
		for (String entry : entries) {
			liqConfDropDown.add(entry);
		}
	}
	
	/**
	 * Removes the given Liquibase configuration from preferences.
	 * 
	 * @param entry the entry
	 */
	public void removeLiqConf(String entry) {
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		if (preferences.nodeExists(Activator.PLUGIN_ID)) {
			ISecurePreferences node = preferences.node(Activator.PLUGIN_ID);
			node.remove("name:" + entry);
			node.remove("user:" + entry);
			node.remove("password" + entry);
			node.remove("changeLogPath:" + entry);
		}
	}

	public void setReleaseButton(Button releaseButton) {
		this.releaseButton = releaseButton;
	}
	
	private void initializeChangeSets(Liquibase liquibase, boolean displayAllChangeSets) throws LiquibaseException {
		if(displayAllChangeSets) {
			List<RanChangeSet> ranChangeSets = liquibase.getDatabase().getRanChangeSetList();
			for (RanChangeSet ranChangeSet : ranChangeSets) {
				ChangeSetContentProvider.getInstance().addChangeSet(new ChangeSet(ranChangeSet.getId(), ChangeSetStatus.FORMER_EXECUTED));
			}
		}
		List<liquibase.changelog.ChangeSet> unRunChangeSets = liquibase.listUnrunChangeSets(null);
		for (liquibase.changelog.ChangeSet unRunChangeSet : unRunChangeSets) {
			 ChangeSetContentProvider.getInstance().addChangeSet(new ChangeSet(unRunChangeSet.getId(), ChangeSetStatus.UNRUN));
		}
	}
	
	private void cleanChangeSets() {
		ChangeSetContentProvider.getInstance().cleanChangeSets();
	}
	
	// re initialize liquibase to avoid problems accessing databasechangelog
	private void initializeLiquibase(boolean isNewConnection) throws SQLException, LiquibaseException {
		if(isNewConnection && connection != null) {
			getConnectionInstance().close();
		}
			database = DatabaseFactory.getInstance()
					.findCorrectDatabaseImplementation(new JdbcConnection(getConnectionInstance()));

		/* Liquibase identifies each ChangeSet by its id, author and file class path
		 * (path from liquibase executable to the specific file)
		 * 
		 * Due to this reason executing ChangeSets or better Logs via the plug-in and ant
		 * leads to different identifiers. The following snippet synchronizes the base path 
		 * with the corresponding ANT-tasks. 
		 * 
		 * This wouldn't be needed with the use of logicalFilePath but you can't link to 
		 * files by setting logicalFilePath. */
		changeLogPath = changeLogPath.replace("\\", "/");
		String[] splittedChangeLogPath = changeLogPath.split(BASE_FOLDER + "/");
		String basePath = splittedChangeLogPath[0] + BASE_FOLDER + "/";
		String changeLogPath = splittedChangeLogPath[1];
		
		liquibase = new Liquibase(changeLogPath, new FileSystemResourceAccessor(basePath), database);
	}
}
