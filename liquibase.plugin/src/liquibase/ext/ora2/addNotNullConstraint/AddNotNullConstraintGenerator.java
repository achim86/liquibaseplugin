package liquibase.ext.ora2.addNotNullConstraint;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;

public class AddNotNullConstraintGenerator extends AbstractSqlGenerator<AddNotNullConstraintStatement> {

    public ValidationErrors validate(AddNotNullConstraintStatement addNotNullConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("tableName", addNotNullConstraintStatement.getTableName());
        validationErrors.checkRequiredField("columnName", addNotNullConstraintStatement.getColumnName());
        validationErrors.checkRequiredField("constraintName", addNotNullConstraintStatement.getConstraintName());

        return validationErrors;
    }

    public Sql[] generateSql(AddNotNullConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();

        sql.append("ALTER TABLE ");
        sql.append(database.escapeTableName(statement.getSchemaName(), statement.getTableName()));
        sql.append(" MODIFY ");
        sql.append(database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()));
        sql.append(" CONSTRAINT ");
        sql.append(statement.getConstraintName());
        sql.append(" NOT NULL");
        return new Sql[] {
                new UnparsedSql(sql.toString())
        };
    }

}
