package liquibase.eclipse.plugin.view.wizards;

import liquibase.eclipse.plugin.controller.LiqConfInspectWizardController;
import liquibase.eclipse.plugin.model.DatabaseConfiguration;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * First page of {@linkliquibase.eclipse.plugin.view.wizards.LiqConfInspectWizard}.
 * Shows details about the selected database configuration.
 * 
 * @author afinke
 *
 */
public class LiqConfInspectPage extends WizardPage {

	private Composite mainComposite;
	private DatabaseConfiguration databaseConfiguration;
	private Text urlText;
	private Text userText;
	private Text passwordText;
	
	public LiqConfInspectPage(String entry) {
		super("Liquibase Configuration");
		setTitle("Liquibase Configuration");
		setDescription(entry);
		LiqConfInspectWizardController liqConfInspectWizardController = 
				new LiqConfInspectWizardController();
		this.databaseConfiguration = liqConfInspectWizardController.getDatabaseConfiguration(entry);
	}

	@Override
	public void createControl(final Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(2, false));
	    GridData mainLayoutData = new GridData(GridData.FILL, GridData.CENTER, true, false);
	    mainComposite.setLayoutData(mainLayoutData); 
	    
		// blank line
		Label blankLine = new Label(mainComposite, SWT.NONE);
		GridData blankLineData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		blankLineData.horizontalSpan = 2;
		blankLine.setLayoutData(blankLineData);
  
	    // URL
	    GridData databaseLabelLayoutData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
	    databaseLabelLayoutData.widthHint = 90;
	    Label urlLabel = new Label(mainComposite, SWT.NONE);
	    urlLabel.setText("URL: ");
	    urlLabel.setLayoutData(databaseLabelLayoutData);
	    GridData textLayoutData = new GridData(GridData.FILL, GridData.CENTER, true, false);
	    urlText = new Text(mainComposite, SWT.BORDER | SWT.SINGLE);
	    urlText.setText(databaseConfiguration.getUrl());
	    urlText.setEditable(false);
	    urlText.setLayoutData(textLayoutData);
	 
		// User
	    Label userLabel = new Label(mainComposite, SWT.NONE);
	    userLabel.setText("User: ");
	    userLabel.setLayoutData(databaseLabelLayoutData);
	    userText = new Text(mainComposite, SWT.BORDER | SWT.SINGLE);
	    userText.setText(databaseConfiguration.getUser());
	    userText.setEditable(false);
	    userText.setLayoutData(textLayoutData);
	    
	    // PW
	    Label passwordLabel = new Label(mainComposite, SWT.NONE);
	    passwordLabel.setText("Password: ");
	    passwordLabel.setLayoutData(databaseLabelLayoutData);
	    passwordText = new Text(mainComposite, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
	    passwordText.setText(databaseConfiguration.getPassword());
	    passwordText.setEditable(false);
	    passwordText.setLayoutData(textLayoutData);
		    
		// Required to avoid an error in the system
		setControl(mainComposite);
		setPageComplete(true);
		
	}

}
