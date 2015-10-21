package org.babyfishdemo.war3shop.db.installer.schema;

import java.util.Collections;
import java.util.List;

/**
 * @author Tao Chen
 */
public abstract class Constraint {

	private Table table;
	
	private List<Column> columns;
	
	Constraint(List<Column> columns) {
		this.table = validate(columns, "columns");
		this.columns = Collections.unmodifiableList(columns);
	}
	
	public Table getTable() {
		return this.table;
	}
	
	public List<Column> getColumns() {
		return this.columns;
	}
	
	static Table validate(List<Column> columns, String paramterName) {
		if (columns == null) {
			throw new IllegalArgumentException("The \"" + paramterName + "\" must not be null");
		}
		if (columns.isEmpty()) {
			throw new IllegalArgumentException("The \"" + paramterName + "\" must not be empty");
		}
		Table table = null;
		for (Column column : columns) {
			if (table == null) {
				table = column.getTable();
			} else if (column.getTable() != table) {
				throw new IllegalArgumentException("All the columns \"" + paramterName + "\" must belong to one table");				
			}
		}
		return table;
	}
}
