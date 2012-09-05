package liquibase.eclipse.plugin.view.wizards;

/**
 * Shared values for {@link liquibase.eclipse.plugin.view.wizards.LiqConfWizard} and 
 * its pages.
 * 
 * @author afinke
 *
 */
public class LiqConfValues {

	private boolean newStructure;
	private boolean existingStructure;
	
	private boolean newStructurePageComplete;
	private boolean existingStructurePageComplete;
	
	public boolean isNewStructure() {
		return newStructure;
	}
	
	public void setNewStructure(boolean newStructure) {
		this.existingStructure = false;
		this.newStructure = newStructure;
	}
	
	public boolean isExistingStructure() {
		return existingStructure;
	}
	
	public void setExistingStructure(boolean existingStructure) {
		this.newStructure = false;
		this.existingStructure = existingStructure;
	}

	public boolean isNewStructurePageComplete() {
		return newStructurePageComplete;
	}

	public void setNewStructurePageComplete(boolean newStructurePageComplete) {
		this.newStructurePageComplete = newStructurePageComplete;
	}

	public boolean isExistingStructurePageComplete() {
		return existingStructurePageComplete;
	}

	public void setExistingStructurePageComplete(
			boolean existingStructurePageComplete) {
		this.existingStructurePageComplete = existingStructurePageComplete;
	}
	
}
