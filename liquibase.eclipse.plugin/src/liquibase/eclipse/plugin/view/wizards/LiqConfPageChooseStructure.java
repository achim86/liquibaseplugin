package liquibase.eclipse.plugin.view.wizards;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * First page of {@link liquibase.eclipse.plugin.view.wizards.LiqConfWizard}.
 * Choose if create new database structure or use existing.
 * 
 * @author afinke
 *
 */
public class LiqConfPageChooseStructure extends WizardPage {

	private LiqConfValues liqConfValues;
	
	private Composite mainComposite;
	private Text nameText;
	private Group radioButtonGroup;
	private Button existingStructure;
	private Button newStructure;

	public LiqConfPageChooseStructure(LiqConfValues liqConfValues) {
		super("Create Liquibase Configuration");
		setTitle("Create Liquibase Configuration");
		this.liqConfValues = liqConfValues;
	}

	@Override
	public void createControl(Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(2, false));
		
		// blank line
		Label blankLine = new Label(mainComposite, SWT.NONE);
		GridData blankLineData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		blankLineData.horizontalSpan = 2;
		blankLine.setLayoutData(blankLineData);
		
		// Name
		Label label1 = new Label(mainComposite, SWT.NONE);
		label1.setText("Name: ");

		nameText = new Text(mainComposite, SWT.BORDER | SWT.SINGLE);
		nameText.setText("");
		nameText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		nameText.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) { }

			@Override
			public void keyReleased(KeyEvent e) {
				validatePage();
			}
		});
		
		// blank line
		blankLine = new Label(mainComposite, SWT.NONE);
		blankLine.setLayoutData(blankLineData);
		
		// Choose
		radioButtonGroup = new Group(mainComposite, SWT.SHADOW_IN);
		radioButtonGroup.setText("Structure: ");
	    radioButtonGroup.setLayout(new GridLayout(1, false));
	    GridData radioButtonLayoutData = new GridData(GridData.FILL, GridData.CENTER, true, false);
	    radioButtonLayoutData.horizontalSpan = 2;
	    radioButtonGroup.setLayoutData(radioButtonLayoutData);
	    newStructure = new Button(radioButtonGroup, SWT.RADIO);
	    newStructure.setText("Create new structure.");
	    existingStructure = new Button(radioButtonGroup, SWT.RADIO);
	    existingStructure.setText("Use existing structure.");

	    SelectionAdapter listener = new SelectionAdapter() {
	    	
	    	@Override
	        public void widgetSelected(SelectionEvent e) {
	            if (isNewStructure()) {
	            	liqConfValues.setNewStructure(true);
	            	validatePage();
	            }
	            
	            if (isExistingStructure()){
	            	liqConfValues.setExistingStructure(true);
	            	validatePage();
	            }
	        }
	    	
	    };
	    newStructure.addSelectionListener(listener);
	    existingStructure.addSelectionListener(listener);
	    
		// Required to avoid an error in the system
		setControl(mainComposite);
		setPageComplete(false);
	}

	private boolean isNewStructure() {
		return newStructure.getSelection();
	}
	
	private boolean isExistingStructure() {
		return existingStructure.getSelection();
	}

	private void validatePage() {
		disableFinish();
		if (!nameText.getText().isEmpty() && (isNewStructure() || isExistingStructure())) {
			setPageComplete(true);
			
			if(isNewStructure() && liqConfValues.isNewStructurePageComplete()) {
				enableFinish();
			}
			
			if(isExistingStructure() && liqConfValues.isExistingStructurePageComplete()) {
				enableFinish();
			}
		} else {
			setPageComplete(false);
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
	
	private void disableFinish(){
		IWizard w = this.getWizard();
		IWizardPage [] pages = w.getPages();
		for(int i = 0; i < pages.length; i++){
			((WizardPage) pages[i]).setPageComplete(false);
		}
		setPageComplete(false); 
	}

	public Text getNameText() {
		return nameText;
	}
	
}
