package liquibase.ext.ora2.dropNotNullConstraint;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;

public class DropNotNullConstraintGenerator extends AbstractSqlGenerator<DropNotNullConstraintStatement> {

    public ValidationErrors validate(DropNotNullConstraintStatement dropNotNullConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("constraintName", dropNotNullConstraintStatement.getTableName());

        return validationErrors;
    }

    public Sql[] generateSql(DropNotNullConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();

        sql.append("ALTER TABLE ");
        sql.append(database.escapeTableName(statement.getSchemaName(), statement.getTableName()));
        sql.append(" DROP CONSTRAINT ");
        sql.append(statement.getConstraintName());
        
        return new Sql[] {
                new UnparsedSql(sql.toString())
        };
    }
}
