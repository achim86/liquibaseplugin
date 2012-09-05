package liquibase.eclipse.plugin.view.wizards;

import liquibase.eclipse.plugin.controller.LiqConfWizardController;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * Initializes database configurations.
 * 
 * @author afinke
 *
 */
public class LiqConfWizard extends Wizard {
	
	private LiqConfWizardController liqConfWizardController;
	
	private LiqConfPageChooseStructure liqConfPageChooseStructure;
	private LiqConfPageNewStructure liqConfPageNewStructure;
	private LiqConfPageExistingStructure liqConfPageExistingStructure;
	private LiqConfValues liqConfValues;

	public LiqConfWizard() {
		super();
		setHelpAvailable(false);
		setWindowTitle("New Liquibase Configuration");
		liqConfWizardController = new LiqConfWizardController();
		// disables the help image in the bottom left; care this is 
		// for all jface dialogs so make sure to activate on cancel/finish
		TrayDialog.setDialogHelpAvailable(false);
	}

	@Override
	public void addPages() {
		liqConfValues = new LiqConfValues();
		liqConfPageChooseStructure = new LiqConfPageChooseStructure(liqConfValues);
		liqConfPageNewStructure = new LiqConfPageNewStructure(liqConfValues);
		liqConfPageExistingStructure = new LiqConfPageExistingStructure(liqConfValues);
		addPage(liqConfPageChooseStructure);
		addPage(liqConfPageNewStructure);
		addPage(liqConfPageExistingStructure);
	}

	@Override
	public boolean performFinish() {
		TrayDialog.setDialogHelpAvailable(true);
		
		if(liqConfValues.isNewStructure()) {
			boolean result = liqConfWizardController.copyStructure(liqConfPageNewStructure.getPathText().getText());
			if(result) {
				liqConfWizardController.storeLiqConf(
						liqConfPageChooseStructure.getNameText().getText(), 
						// needs to be adjusted if the template changes
						liqConfPageNewStructure.getPathText().getText() + "/database/changelogs/release.xml",
						liqConfPageNewStructure.getUrlText().getText(),
						liqConfPageNewStructure.getUserText().getText(),
						liqConfPageNewStructure.getPasswordText().getText()
				);
				return true;
			} else {
				MessageDialog.openError(getShell(), "Error", "Delete the existing directory first.");
				return false;
			}
		}
		
		if(liqConfValues.isExistingStructure()) {
			liqConfWizardController.storeLiqConf(
					liqConfPageChooseStructure.getNameText().getText(), 
					liqConfPageExistingStructure.getPathText().getText(),
					liqConfPageExistingStructure.getUrlText().getText(),
					liqConfPageExistingStructure.getUserText().getText(),
					liqConfPageExistingStructure.getPasswordText().getText()
			);
		}
		return true;
	}
	
	@Override
    public IWizardPage getNextPage(IWizardPage currentPage) {
        if (liqConfValues.isNewStructure()) {
            return liqConfPageNewStructure;
        }
        
        if (liqConfValues.isExistingStructure()) {
            return liqConfPageExistingStructure;
        }
        
        return null;
    }
	
	@Override
	public boolean performCancel() {
		TrayDialog.setDialogHelpAvailable(true);
		return super.performCancel();
	}

	public LiqConfPageNewStructure getLiqConfPageNewStructure() {
		return liqConfPageNewStructure;
	}

	public LiqConfPageExistingStructure getLiqConfPageExistingStructure() {
		return liqConfPageExistingStructure;
	}

}
