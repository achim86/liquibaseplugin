package liquibase.eclipse.plugin.model;

/**
 * Entity class for database configurations.
 * 
 * @author afinke
 *
 */
public class DatabaseConfiguration {
	private String url;
	private String user;
	private String password;
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
}
