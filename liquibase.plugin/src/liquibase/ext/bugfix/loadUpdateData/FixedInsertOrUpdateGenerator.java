package liquibase.ext.bugfix.loadUpdateData;

import java.util.Arrays;
import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.sqlgenerator.core.InsertGenerator;
import liquibase.sqlgenerator.core.UpdateGenerator;
import liquibase.statement.core.UpdateStatement;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

import java.util.Date;
import java.util.HashSet;

/**
 * Temporarily bugfix for https://liquibase.jira.com/browse/CORE-1170.
 * 
 * @author afinke
 *
 */
public class FixedInsertOrUpdateGenerator extends AbstractSqlGenerator<FixedInsertOrUpdateStatement> {

	@Override
    public int getPriority() {
        return PRIORITY_DEFAULT + 1;
    }
	
    public ValidationErrors validate(FixedInsertOrUpdateStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columns", statement.getColumnValues());
        validationErrors.checkRequiredField("primaryKey", statement.getPrimaryKey());

        return validationErrors;
    }

    public Sql[] generateSql(FixedInsertOrUpdateStatement fixedInsertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
       
    	StringBuffer completeSql = new StringBuffer();
        String whereClause = getWhereClause(fixedInsertOrUpdateStatement, database);

        completeSql.append( getRecordCheck(fixedInsertOrUpdateStatement, database, whereClause));

        completeSql.append(getInsertStatement(fixedInsertOrUpdateStatement, database, sqlGeneratorChain));

        try {
            String updateStatement = getUpdateStatement(fixedInsertOrUpdateStatement,database,whereClause,sqlGeneratorChain);
            completeSql.append(getElse(database));
            completeSql.append(updateStatement);
        } catch (LiquibaseException e) {}
        completeSql.append(getPostUpdateStatements());
        return new Sql[]{
                new UnparsedSql(completeSql.toString())
        };
    }
    
    private String getRecordCheck(FixedInsertOrUpdateStatement fixedInsertOrUpdateStatement, Database database, String whereClause) {

        StringBuffer recordCheckSql = new StringBuffer();

        recordCheckSql.append("DECLARE\n");
        recordCheckSql.append("\tv_reccount NUMBER := 0;\n");
        recordCheckSql.append("BEGIN\n");
        recordCheckSql.append("\tSELECT COUNT(*) INTO v_reccount FROM " + database.escapeTableName(fixedInsertOrUpdateStatement.getSchemaName(), fixedInsertOrUpdateStatement.getTableName()) + " WHERE ");

        recordCheckSql.append(whereClause);
        recordCheckSql.append(";\n");

        recordCheckSql.append("\tIF v_reccount = 0 THEN\n");

        return recordCheckSql.toString();
    }
	
	private String getElse(Database database){
        return "\tELSIF v_reccount = 1 THEN\n";
	}

	private String getPostUpdateStatements(){
        StringBuffer endStatements = new StringBuffer();
        endStatements.append("END IF;\n");
        endStatements.append("END;\n");
        return endStatements.toString();
    }
    
    private String getWhereClause(FixedInsertOrUpdateStatement fixedInsertOrUpdateStatement, Database database) {
        StringBuffer where = new StringBuffer();

        String[] pkColumns = fixedInsertOrUpdateStatement.getPrimaryKey().split(",");

        for(String thisPkColumn:pkColumns)
        {
            where.append(database.escapeColumnName(fixedInsertOrUpdateStatement.getSchemaName(), fixedInsertOrUpdateStatement.getTableName(), thisPkColumn)).append(" = ");
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

    private String getInsertStatement(FixedInsertOrUpdateStatement fixedInsertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer insertBuffer = new StringBuffer();
        InsertGenerator insert = new InsertGenerator();
        Sql[] insertSql = insert.generateSql(fixedInsertOrUpdateStatement,database,sqlGeneratorChain);

        for(Sql s:insertSql)
        {
            insertBuffer.append(s.toSql());
            insertBuffer.append(";");
        }

        insertBuffer.append("\n");

        return insertBuffer.toString();
    }

    /**
     * 
     * @param fixedInsertOrUpdateStatement
     * @param database
     * @param whereClause
     * @param sqlGeneratorChain
     * @return the update statement, if there is nothing to update return null
     */
    private String getUpdateStatement(FixedInsertOrUpdateStatement fixedInsertOrUpdateStatement,Database database, String whereClause, SqlGeneratorChain sqlGeneratorChain) throws LiquibaseException {

        StringBuffer updateSqlString = new StringBuffer();

        UpdateGenerator update = new UpdateGenerator();
        UpdateStatement updateStatement = new UpdateStatement(fixedInsertOrUpdateStatement.getSchemaName(),fixedInsertOrUpdateStatement.getTableName());
        updateStatement.setWhereClause(whereClause + ";\n");

        String[] pkFields=fixedInsertOrUpdateStatement.getPrimaryKey().split(",");
        HashSet<String> hashPkFields = new HashSet<String>(Arrays.asList(pkFields));
        for(String columnKey:fixedInsertOrUpdateStatement.getColumnValues().keySet())
        {
            if (!hashPkFields.contains(columnKey)) {
                updateStatement.addNewColumnValue(columnKey,fixedInsertOrUpdateStatement.getColumnValue(columnKey));
            }
        }
        // this isn't very elegant but the code fails above without any columns to update
        if(updateStatement.getNewColumnValues().isEmpty()) {
        	throw new LiquibaseException("No fields to update in set clause");
        }

        Sql[] updateSql = update.generateSql(updateStatement, database, sqlGeneratorChain);

        for(Sql s:updateSql)
        {
            updateSqlString.append(s.toSql());
            updateSqlString.append(";");
        }

        updateSqlString.deleteCharAt(updateSqlString.lastIndexOf(";"));
        updateSqlString.append("\n");

        return updateSqlString.toString();

    }
}
