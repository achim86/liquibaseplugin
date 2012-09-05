package liquibase.ext.bugfix.loadUpdateData;

import liquibase.change.CheckSum;
import liquibase.change.core.LoadDataChange;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.util.csv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FixedLoadUpdateDataChange extends LoadDataChange {
    private String primaryKey;
    private Boolean relativeToChangelogFile;

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return super.generateStatements(database);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public FixedLoadUpdateDataChange() {
        super("fixedLoadUpdateData", "Smart Load Data");
    }


    public void setPrimaryKey(String primaryKey) throws LiquibaseException {
        if (primaryKey == null) {
            throw new LiquibaseException("primaryKey cannot be null.");
        }
        this.primaryKey = primaryKey;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }
    
    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    public LoadDataColumnConfig createColumn() {
        final LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
        super.addColumn(columnConfig);
        return columnConfig;
    }
    
    @Override
    protected InsertStatement createStatement(String schemaName, String tableName) {
    	FixedInsertOrUpdateStatement statement = new FixedInsertOrUpdateStatement(schemaName, tableName, this.primaryKey);
    	return statement;
    }

    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        SqlStatement[] forward = this.generateStatements(database);

        for(SqlStatement thisForward: forward){
            FixedInsertOrUpdateStatement thisInsert = (FixedInsertOrUpdateStatement)thisForward;
            DeleteStatement delete = new DeleteStatement(getSchemaName(),getTableName());
            delete.setWhereClause(getWhereClause(thisInsert,database));
            statements.add(delete);
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private String getWhereClause(FixedInsertOrUpdateStatement fixedInsertOrUpdateStatement, Database database) {
    	StringBuffer where = new StringBuffer();

        String[] pkColumns = fixedInsertOrUpdateStatement.getPrimaryKey().split(",");

        for(String thisPkColumn:pkColumns)
        {
            where.append(database.escapeColumnName(fixedInsertOrUpdateStatement.getSchemaName(), fixedInsertOrUpdateStatement.getTableName(), thisPkColumn)  + " = " );
            Object newValue = fixedInsertOrUpdateStatement.getColumnValues().get(thisPkColumn);
            if (newValue == null || newValue.toString().equals("NULL")) {
                where.append("NULL");
            } else if (newValue instanceof String && database.shouldQuoteValue(((String) newValue))) {
                where.append("'").append(database.escapeStringForDatabase((String) newValue)).append("'");
            } else if (newValue instanceof Date) {
                where.append(database.getDateLiteral(((Date) newValue)));
            } else if (newValue instanceof Boolean) {
                if (((Boolean) newValue)) {
                    where.append(TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getTrueBooleanValue());
                } else {
                    where.append(TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getFalseBooleanValue());
                }
            } else {
                where.append(newValue);
            }

            where.append(" AND ");
        }

        where.delete(where.lastIndexOf(" AND "),where.lastIndexOf(" AND ") + " AND ".length());
        return where.toString();
    }

    @Override
    protected CSVReader getCSVReader() throws IOException {
    	String file = getFile();
    	if (relativeToChangelogFile != null && relativeToChangelogFile) {
    		if(getChangeSet().getFilePath().contains("/")) {
    			file = getChangeSet().getFilePath().replaceFirst("/[^/]*$", "") + "/" + getFile();
    		}
    	}

    	ResourceAccessor opener = getResourceAccessor();
        if (opener == null) {
            throw new UnexpectedLiquibaseException("No file opener specified for "+file);
        }
        InputStream stream = opener.getResourceAsStream(file);
        if (stream == null) {
            throw new UnexpectedLiquibaseException("Data file "+file+" was not found");
        }

        InputStreamReader streamReader;
        if (getEncoding() == null) {
            streamReader = new InputStreamReader(stream);
        } else {
            streamReader = new InputStreamReader(stream, getEncoding());
        }

        char quotchar;
        if (0 == this.getQuotchar().length() ) {
        	// hope this is impossible to have a field surrounded with non ascii char 0x01
        	quotchar = '\1';
        } else {
        	quotchar = this.getQuotchar().charAt(0);
        }

        CSVReader reader = new CSVReader(streamReader, getSeparator().charAt(0), quotchar );

        return reader;
    }
    
    @Override
    public CheckSum generateCheckSum() {
    	String file = getFile();
    	if (relativeToChangelogFile != null && relativeToChangelogFile) {
    		if(getChangeSet().getFilePath().contains("/")) {
    			file = getChangeSet().getFilePath().replaceFirst("/[^/]*$", "") + "/" + getFile();
    		}
    	}
        InputStream stream = null;
        try {
            stream = getResourceAccessor().getResourceAsStream(file);
            if (stream == null) {
                throw new RuntimeException(file + " could not be found");
            }
            return CheckSum.compute(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    ;
                }
            }
        }
    }
}
