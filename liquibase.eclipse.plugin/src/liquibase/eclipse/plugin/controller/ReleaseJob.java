package liquibase.eclipse.plugin.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import liquibase.Liquibase;
import liquibase.eclipse.plugin.model.DatabaseConfiguration;
import liquibase.exception.LiquibaseException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;


/**
 * Handles the multiple threads started by a liquibase release.
 * 
 * @author afinke
 *
 */
public class ReleaseJob extends Job {

	private Liquibase liquibase;
	private DatabaseConfiguration databaseConfiguration;
	private Button releaseButton;
	
	public ReleaseJob(String name, 
					  DatabaseConfiguration databaseConfiguration,
					  Liquibase liquibase, 
					  Button releaseButton) {
		super(name);
		this.liquibase = liquibase;
		this.databaseConfiguration = databaseConfiguration;
		this.releaseButton = releaseButton;
	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// disable release button to avoid multiple releases
				releaseButton.setEnabled(false);
			}
		});
		ExecutorService executor = Executors.newCachedThreadPool();
		DatabaseChangelogPoller databaseChangelogPoller = 
				new DatabaseChangelogPoller(databaseConfiguration);
		// start polling 'databasechangelog'
		executor.execute(databaseChangelogPoller);
		try {
			// start updating the database
			liquibase.update(null);
		} catch (LiquibaseException e) {
			e.printStackTrace();
		}
		databaseChangelogPoller.stop();
		executor.shutdown();
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// enable release button after everything finished
				releaseButton.setEnabled(true);
			}
		});
		return Status.OK_STATUS;
	}
	
}
