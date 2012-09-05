package liquibase.eclipse.plugin.controller;

import liquibase.eclipse.plugin.Activator;
import liquibase.eclipse.plugin.model.DatabaseConfiguration;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

/**
 * Controller for {@link liquibase.eclipse.plugin.view.wizards.LiqConfInspectWizard}.
 * 
 * @author afinke
 *
 */
public class LiqConfInspectWizardController {

	/**
	 * Reads the database configuration for the given entry.
	 * 
	 * @param entry the entry
	 * @return the database configuration
	 */
	public DatabaseConfiguration getDatabaseConfiguration(String entry) {
		DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		if (preferences.nodeExists(Activator.PLUGIN_ID)) {
			ISecurePreferences node = preferences.node(Activator.PLUGIN_ID);
			for (String key : node.keys()) {
				try {
					if (key.startsWith("url:" + entry)) {
						databaseConfiguration.setUrl(node.get(key, "n/a"));
					}
					if (key.startsWith("user:" + entry)) {
						databaseConfiguration.setUser(node.get(key, "n/a"));
					}
					if (key.startsWith("password:" + entry)) {
						databaseConfiguration.setPassword(node.get(key, "n/a"));
					}
				} catch (StorageException e) {
					e.printStackTrace();
				}
			}
		}
		return databaseConfiguration;
	}
}
