package org.babyfishdemo.war3shop.db.installer.executor;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.babyfishdemo.war3shop.db.installer.schema.Table;
import org.babyfishdemo.war3shop.db.installer.schema.Type;

/**
 * @author Tao Chen
 */
public class HsqldbExecutor extends Executor {

	public HsqldbExecutor(String dir, DataSource dataSource) {
		super(dir, dataSource);
	}

	@Override
	protected String mapType(Type type) {
		switch (type) {
		case BOOLEAN:
			return "BOOLEAN";
		case INT:
			return "INTEGER";
		case LONG:
			return "BIGINT";
		case DECIMAL:
			return "NUMERIC";
		case DATE:
			return "DATE";
		case TIMESTAMP:
			return "TIMESTAMP";
		case STRING:
			return "VARCHAR";
		case BINARY:
			return "VARBINARY";
		case CLOB:
			return "CLOB";
		case BLOB:
			return "BLOB";
		}
		throw new IllegalArgumentException("Invalid argument");
	}

	@Override
	protected void dropObjectsIfExsits(Table table) throws SQLException {
		this.executeDDL(
				"DROP TABLE \""
				+ table.getName()
				+ "\" IF EXISTS CASCADE");
		this.executeDDL(
				"DROP SEQUENCE \""
				+ table.getSequenceName()
				+ "\" IF EXISTS CASCADE");
	}

}
