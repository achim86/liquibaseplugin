package liquibase.eclipse.plugin.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import liquibase.eclipse.plugin.Activator;
import liquibase.eclipse.plugin.model.DatabaseConfiguration;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.osgi.framework.Bundle;


/**
 * Controller for {@link liquibase.eclipse.plugin.view.wizards.LiqConfWizard}.
 * 
 * @author afinke
 * 
 */
public class LiqConfWizardController {
	
	public LiqConfWizardController() {
		
	}
	
	/**
	 * Stores the given parameters in a liquibase configuration.
	 * 
	 * @param name the name
	 * @param changeLogPath the change log path
	 * @param url the url
	 * @param user the user
	 * @param password the password
	 */
	public void storeLiqConf(String name, 
							 String changeLogPath, 
							 String url,
							 String user, 
							 String password) {
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		ISecurePreferences node = preferences.node(Activator.PLUGIN_ID);
		try {
			node.put("name:" + name, name, true);
			node.put("changeLogPath:" + name, changeLogPath, true);
			node.put("url:" + name, url, true);
			node.put("user:" + name, user, true);
			node.put("password:" + name, password, true);
		} catch (StorageException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper for using import of a database configuration via a properties file.
	 * 
	 * @param fileName the file name
	 * @param prefix the prefix
	 * @return the database configuration
	 */
	public DatabaseConfiguration handleProperties(String fileName, String prefix) {
		DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
		if(0 < prefix.length()) {
			prefix = prefix + ".";
		}
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				line.replaceAll(" ", "");
				if(line.startsWith(prefix + "url")) {
					databaseConfiguration.setUrl(line.substring(prefix.length() + 4).trim());
				}
				if(line.startsWith(prefix + "username")) {
					databaseConfiguration.setUser(line.substring(prefix.length() + 9).trim());
				}
				if(line.startsWith(prefix + "password")) {
					databaseConfiguration.setPassword(line.substring(prefix.length() + 9).trim());
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return databaseConfiguration;
	}
	
	/**
	 * Copies the structure from template to the given project path.
	 * 
	 * @param projectPath the project path
	 * @return false if the directory already exists; otherwise true
	 */
	public boolean copyStructure(String projectPath) {
		if (new File(projectPath + "/database").exists()) {
			return false;
		}
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		File file = new File(projectPath + "/database.zip");
		try {
			InputStream inputStream = 
					FileLocator.openStream(bundle, new Path("/templates/database.zip"), false);
			FileUtils.copyInputStreamToFile(inputStream, file);
			unzip(file);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		file.delete();
		return true;
	}
	 
	/**
	 * Unzips the zipped template.
	 * 
	 * @author http://www.java-examples.com
	 * @author afinke
	 * 
	 * @param zipFileName
	 */
	private void unzip(File file) {

		try {
			String zipPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4);
			File temp = new File(zipPath);
			temp.mkdir();

			ZipFile zipFile = new ZipFile(file);
			Enumeration<?> e = zipFile.entries();

			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				File destinationFilePath = new File(zipPath, entry.getName());

				destinationFilePath.getParentFile().mkdirs();

				if (entry.isDirectory()) {
					continue;
				} else {

					BufferedInputStream bis = new BufferedInputStream(
							zipFile.getInputStream(entry));

					int b;
					byte buffer[] = new byte[1024];

					FileOutputStream fos = 
							new FileOutputStream(destinationFilePath);
					BufferedOutputStream bos = 
							new BufferedOutputStream(fos, 1024);

					while ((b = bis.read(buffer, 0, 1024)) != -1) {
						bos.write(buffer, 0, b);
					}

					bos.flush();
					bos.close();
					bis.close();
				}
			}
			
			zipFile.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
