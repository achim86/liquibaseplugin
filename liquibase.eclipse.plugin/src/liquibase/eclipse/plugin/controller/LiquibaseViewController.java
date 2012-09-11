package liquibase.eclipse.plugin.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.eclipse.plugin.Activator;
import liquibase.eclipse.plugin.model.ChangeSet;
import liquibase.eclipse.plugin.model.ChangeSetContentProvider;
import liquibase.eclipse.plugin.model.DatabaseConfiguration;
import liquibase.eclipse.plugin.model.ChangeSetStatus;
import liquibase.exception.DatabaseException;
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
	public static final String oracleDriver = "oracle.jdbc.OracleDriver";

	private Connection con = null;
	private Database database;
	private DatabaseConfiguration databaseConfiguration;
	private String changeLogPath;
	private Liquibase liquibase;
	private Button releaseButton;

	public LiquibaseViewController() {
		
	}
	
	/**
	 * Initializes Liquibase with the given change log.
	 * 
	 * @param changeLogPath the change log path
	 * @param databaseConfiguration the database configuration
	 * @throws LiquibaseException
	 */
	public void initChangeLog(String changeLogPath, DatabaseConfiguration databaseConfiguration) throws LiquibaseException {
		this.changeLogPath = changeLogPath;
		this.databaseConfiguration = databaseConfiguration;
		try {
			Class.forName(oracleDriver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			con = DriverManager.getConnection(
					databaseConfiguration.getUrl(), 
					databaseConfiguration.getUser(), 
					databaseConfiguration.getPassword());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		cleanChangeSets();
		initializeLiquibase(con);
		initUnrunChangeSets(liquibase);

	}
	
	/**
	 * Starts {@link liquibase.eclipse.plugin.controller.RelaseJob}. 
	 */
	public void release(Shell shell) {
		initializeLiquibase(con);
		ReleaseJob releaseJob = new ReleaseJob("Liquibase Release", databaseConfiguration, liquibase, releaseButton, shell);
		releaseJob.schedule();
	}
	
	public String getLastVersion() {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = ((JdbcConnection) database.getConnection())
					.getWrappedConnection();
			// get the latest tag (which is number 2 in the tag order)
			String sql = "SELECT tag FROM( " + 
						    "SELECT tag, ROWNUM rn FROM( " +
						        "SELECT DISTINCT tag " +
						        "FROM databasechangelog " +
						        "WHERE tag IS NOT NULL " +
						        "ORDER BY tag DESC)) " +
						  "WHERE rn = 2";
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql.toString());
			while (resultSet.next()) {
				String tmpTag = resultSet.getString("tag");
				return tmpTag;
			}
			// special case deploy from version 0 breaks
			sql = "SELECT DISTINCT tag " +
				  "FROM databasechangelog " +
				  "WHERE tag IS NOT NULL";
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql.toString());
			while (resultSet.next()) {
				String tmpTag = resultSet.getString("tag");
				return tmpTag;
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
		return null;
	}
	
	/**
	 * Starts Liquibase rollback to the given version.
	 * 
	 * @param version the version
	 */
	public void restore(String version) {
		initializeLiquibase(con);
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
	
	private void initUnrunChangeSets(Liquibase liquibase) throws LiquibaseException {
		List<liquibase.changelog.ChangeSet> unRunChangeSets = liquibase.listUnrunChangeSets(null);
		for (liquibase.changelog.ChangeSet unRunChangeSet : unRunChangeSets) {
			 ChangeSetContentProvider.getInstance().addChangeSet(new ChangeSet(unRunChangeSet.getId(), ChangeSetStatus.UNRUN));
		}
	}
	
	private void cleanChangeSets() {
		ChangeSetContentProvider.getInstance().cleanChangeSets();
	}
	
	// re initialize liquibase to avoid problems accessing databasechangelog
	private void initializeLiquibase(Connection con) {
		try {
			database = DatabaseFactory.getInstance()
					.findCorrectDatabaseImplementation(new JdbcConnection(con));
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		try {
			liquibase = new Liquibase(changeLogPath, new FileSystemResourceAccessor(), database);
		} catch (LiquibaseException e) {
			e.printStackTrace();
		}
	}
}
