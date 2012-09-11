package liquibase.eclipse.plugin.model;

/**
 * Different status for change sets.
 * 
 * @author afinke
 *
 */
public enum ChangeSetStatus {
	
	UNRUN,
	RUNNING,
	EXECUTED,
	ERROR

};
