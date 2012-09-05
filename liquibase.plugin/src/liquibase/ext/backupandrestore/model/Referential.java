package liquibase.ext.backupandrestore.model;

/**
 * Entity class for referentials.
 * 
 * @author afinke
 *
 */
public class Referential {

	private String constraintName;
	private String srcTableName;
	private String srcColumnName;
	private String refTableName;
	private String refColumnName;
	
	public String getConstraintName() {
		return constraintName;
	}
	
	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}
	
	public String getSrcTableName() {
		return srcTableName;
	}
	
	public void setSrcTableName(String srcTableName) {
		this.srcTableName = srcTableName;
	}
	
	public String getSrcColumnName() {
		return srcColumnName;
	}
	
	public void setSrcColumnName(String srcColumnName) {
		this.srcColumnName = srcColumnName;
	}
	
	public String getRefTableName() {
		return refTableName;
	}
	
	public void setRefTableName(String refTableName) {
		this.refTableName = refTableName;
	}
	
	public String getRefColumnName() {
		return refColumnName;
	}

	public void setRefColumnName(String refColumnName) {
		this.refColumnName = refColumnName;
	}
	
}
