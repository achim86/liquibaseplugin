package liquibase.ext.bugfix.database;

import liquibase.database.core.OracleDatabase;

/**
 * Temporarily bugfix for https://liquibase.jira.com/browse/CORE-1190.
 * 
 * @author afinke
 *
 */
public class FixedOracleDatabase extends OracleDatabase {
	
	@Override
	public int getPriority() {
        return PRIORITY_DEFAULT + 1;
    }
	
	@Override
    public boolean isReservedWord(String objectName) {
		return ("size".equalsIgnoreCase(objectName) || 
			   ("start").equals(objectName) || 
			   super.isReservedWord(objectName));
    }
	
}
