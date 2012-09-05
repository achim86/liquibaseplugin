package liquibase.eclipse.plugin.model;


/**
 * Wrapper for Liquibase ChangeSets.
 * 
 * @author afinke
 *
 */
public class ChangeSet extends AbstractModelObject {

	private String id;
	private volatile Status status;
	private long executionTime;

	public ChangeSet(String id, Status status) {
		this.id = id;
		this.status = status;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		firePropertyChange("id", this.id, this.id = id);
	}

	public synchronized Status getStatus() {
		return status;
	}

	public synchronized void setStatus(Status status) {
		firePropertyChange("status", this.status, this.status = status);
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}
	
}
