package liquibase.eclipse.plugin.view.wizards;

import liquibase.eclipse.plugin.controller.LiqConfWizardController;
import liquibase.eclipse.plugin.model.DatabaseConfiguration;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Second page of {@link liquibase.eclipse.plugin.view.wizards.LiqConfWizard}.
 * Configurate new strucuture.
 * 
 * @author afinke
 *
 */
public class LiqConfPageNewStructure extends WizardPage {
	private Text urlText;
	private Text userText;
	private Text passwordText;
	private Text pathText;
	private Composite mainComposite;
	private LiqConfValues liqConfValues;
	private String workspacePath;
	private LiqConfWizardController liqConfWizardController;

	public LiqConfPageNewStructure(LiqConfValues liqConfValues) {
		super("Create Liquibase Configuration");
		setTitle("Create Liquibase Configuration");
		setDescription("Create Liquibase configuration including structure.");
		this.liqConfValues = liqConfValues;
		workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		liqConfWizardController = new LiqConfWizardController();
	}

	@Override
	public void createControl(final Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, false));
		
		// blank line
		Label blankLine = new Label(mainComposite, SWT.NONE);
		GridData blankLineData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		blankLineData.horizontalSpan = 2;
		blankLine.setLayoutData(blankLineData);
		
		// database connection
		Group databaseConnection = new Group(mainComposite, SWT.SHADOW_IN);
		databaseConnection.setText("Database Connection Information: ");
		databaseConnection.setLayout(new GridLayout(3, false));
	    GridData databaseConnectionLayoutData = new GridData(GridData.FILL, GridData.CENTER, true, false);
	    databaseConnection.setLayoutData(databaseConnectionLayoutData);   
	    // URL
	    GridData databaseLabelLayoutData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
	    databaseLabelLayoutData.widthHint = 67;
	    Label urlLabel = new Label(databaseConnection, SWT.NONE);
	    urlLabel.setText("URL: ");
	    urlLabel.setLayoutData(databaseLabelLayoutData);
	    GridData textLayoutData = new GridData(GridData.FILL, GridData.CENTER, true, false);
	    //textLayoutData.widthHint = 250;
	    urlText = new Text(databaseConnection, SWT.BORDER | SWT.SINGLE);
	    urlText.setText("jdbc:oracle:thin:@[HOST][:PORT]:SID");
	    urlText.setLayoutData(textLayoutData);
	    urlText.addKeyListener(new KeyReleaseListener());
	    Button importButton = new Button(databaseConnection, SWT.PUSH);
	    importButton.setText("Import");
	    GridData importButtonLayoutData = new GridData(GridData.FILL, GridData.CENTER, false, false);
	    importButtonLayoutData.widthHint = 55;
	    importButtonLayoutData.verticalSpan = 3;
	    importButton.setLayoutData(importButtonLayoutData);
	    importButton.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		String prefix = "";
	    		InputDialog inputDialog = new InputDialog(parent.getShell(), "Define Stage", "Properties should look like the following for e.g. stage 'dev':\ndev.url : theUrl\ndev.user : theUser\ndev.password : thePassword\n\n Leave the field empty if stages are not separated.", "", null);
	    		if(Window.OK == inputDialog.open()) {
	    			prefix = inputDialog.getValue();
		    		FileDialog fileDialog = new FileDialog(parent.getShell());
		    		fileDialog.setText("Select File");
		    		fileDialog.setFilterExtensions(new String[] { "*.properties" });
		    		fileDialog.setFilterNames(new String[] { "Liquibase Properties(*.properties)" });
		    		fileDialog.setFilterPath(workspacePath);
		    		String fileName = fileDialog.open();
		    		if(fileName != null) {
		    			DatabaseConfiguration databaseConfiguration = 
		    					liqConfWizardController.handleProperties(fileName, prefix);
		    			urlText.setText(databaseConfiguration.getUrl());
		    			userText.setText(databaseConfiguration.getUser());
		    			passwordText.setText(databaseConfiguration.getPassword());
		    		}
		    		validatePage();
	    		}
	    	}
		});
	    // User
	    Label userLabel = new Label(databaseConnection, SWT.NONE);
	    userLabel.setText("User: ");
	    userLabel.setLayoutData(databaseLabelLayoutData);
	    userText = new Text(databaseConnection, SWT.BORDER | SWT.SINGLE);
	    userText.setText("");
	    userText.setLayoutData(textLayoutData);
	    userText.addKeyListener(new KeyReleaseListener());
	    // PW
	    Label passwordLabel = new Label(databaseConnection, SWT.NONE);
	    passwordLabel.setText("Password: ");
	    passwordLabel.setLayoutData(databaseLabelLayoutData);
	    passwordText = new Text(databaseConnection, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
	    passwordText.setText("");
	    passwordText.setLayoutData(textLayoutData);
	    passwordText.addKeyListener(new KeyReleaseListener());
	    
	    // blank line
 		blankLine = new Label(mainComposite, SWT.NONE);
 		blankLine.setLayoutData(blankLineData);
	    
	    // project path
	    Group projectInfo = new Group(mainComposite, SWT.SHADOW_IN);
	    projectInfo.setText("Eclipse Project Information: ");
	    projectInfo.setLayout(new GridLayout(3, false));
	    GridData projectInfoLayoutData = new GridData(GridData.FILL, GridData.CENTER, true, false);
	    projectInfo.setLayoutData(projectInfoLayoutData);  
	    // Path
	    Label pathLabel = new Label(projectInfo, SWT.NONE);
	    pathLabel.setText("Project Path: ");
	    pathText = new Text(projectInfo, SWT.BORDER | SWT.SINGLE);
	    pathText.setText("");
	    pathText.setLayoutData(textLayoutData);
	    pathText.addKeyListener(new KeyReleaseListener());
	    GridData openButtonLayoutData = new GridData(GridData.FILL, GridData.CENTER, false, false);
	    openButtonLayoutData.widthHint = 55;
	    Button openButton = new Button(projectInfo, SWT.PUSH);
	    openButton.setText("Open");
	    openButton.setLayoutData(openButtonLayoutData);
	    openButton.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		Shell shell = parent.getShell();
	    		DirectoryDialog directoryDialog = new DirectoryDialog(shell);
	    		directoryDialog.setText("Select Directory");
	    		directoryDialog.setFilterPath(workspacePath);
	    		String directoryName = directoryDialog.open();
	    		if(directoryName != null) {
	    			pathText.setText(directoryName);
	    		}
	    		validatePage();
	    	}
		});
	    
		// Required to avoid an error in the system
		setControl(mainComposite);
		setPageComplete(false);
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return false;
	}
	
	public void validatePage() {
		if (!urlText.getText().isEmpty() &&
			!userText.getText().isEmpty() &&
			!passwordText.getText().isEmpty() &&
			!pathText.getText().isEmpty()
		) {
			enableFinish();
			liqConfValues.setNewStructurePageComplete(true);
		} else {
			setPageComplete(false);
			liqConfValues.setNewStructurePageComplete(false);
		}
	}
	
	private void enableFinish(){
		IWizard w = this.getWizard();
		IWizardPage [] pages = w.getPages();
		for(int i = 0; i < pages.length; i++){
			((WizardPage) pages[i]).setPageComplete(true);
		}
		setPageComplete(true); 
	}

	public class KeyReleaseListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) { }

		@Override
		public void keyReleased(KeyEvent e) {
			validatePage();
		}
	}

	public Text getUrlText() {
		return urlText;
	}

	public Text getUserText() {
		return userText;
	}

	public Text getPasswordText() {
		return passwordText;
	}

	public Text getPathText() {
		return pathText;
	}
	
}
