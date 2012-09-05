package liquibase.eclipse.plugin.view.wizards;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.wizard.Wizard;

/**
 * Shows details about the selected database configuration.
 * 
 * @author afinke
 *
 */
public class LiqConfInspectWizard extends Wizard {

	private LiqConfInspectPage liqConfInspectPage;
	private String entry;
	
	public LiqConfInspectWizard(String entry) {
		setHelpAvailable(false);
		setWindowTitle("Inspect Liquibase Configuration");
		this.entry = entry;
		// disables the help image in the bottom left; care this is 
		// for all jface dialogs so make sure to activate on cancel/finish
		TrayDialog.setDialogHelpAvailable(false);
	}
	
	@Override
	public void addPages() {
		liqConfInspectPage = new LiqConfInspectPage(entry);
		addPage(liqConfInspectPage);
	}
	
	@Override
	public boolean performFinish() {
		TrayDialog.setDialogHelpAvailable(true);
		return true;
	}
	
	@Override
	public boolean performCancel() {
		TrayDialog.setDialogHelpAvailable(true);
		return super.performCancel();
	}

}
