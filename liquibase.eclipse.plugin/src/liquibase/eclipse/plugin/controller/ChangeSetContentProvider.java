package liquibase.eclipse.plugin.controller;

import java.util.LinkedList;
import java.util.List;

import liquibase.eclipse.plugin.model.ChangeSet;

import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;

/**
 * Provides the wrapped ChangeSets for the View.
 * Implemented as a singleton.
 * 
 * @author afinke
 *
 */
public class ChangeSetContentProvider extends ObservableListContentProvider {

	private static ChangeSetContentProvider changeSetContentProvider;
	private List<ChangeSet> changeSets;
	
	private ChangeSetContentProvider() {
		changeSets = new LinkedList<ChangeSet>();
	}
	
	public synchronized static ChangeSetContentProvider getInstance() {
		if(changeSetContentProvider == null) {
			changeSetContentProvider = new ChangeSetContentProvider();
		}
		return changeSetContentProvider;
	}
	
	public void addChangeSet(ChangeSet changeSet) {
		this.changeSets.add(changeSet);
	}
	
	public void removeChangeSet(ChangeSet changeSet) {
		this.changeSets.remove(changeSet);
	}
	
	public void removeChangeSet(int i) {
		this.changeSets.remove(i);
	}
	
	public List<ChangeSet> getChangeSets() {
		return changeSets;
	}
	
	public void cleanChangeSets() {
		changeSets = new LinkedList<ChangeSet>();
	}
}
