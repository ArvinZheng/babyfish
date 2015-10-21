package org.babyfishdemo.war3shop.db.installer.schema;

import java.util.Collections;
import java.util.List;

/**
 * @author Tao Chen
 */
public class ForeignKeyConstraint extends Constraint {

	private Table referencedTable;
	
	private List<Column> referencedColumns;
	
	ForeignKeyConstraint(List<Column> columns, List<Column> referencedColumns) {
		super(columns);
		if (columns.size() != referencedColumns.size()) {
			throw new IllegalArgumentException("The size of columns and referencedColumns must be equal");
		}
		this.referencedTable = validate(referencedColumns, "referencedColumns");
		this.referencedColumns = Collections.unmodifiableList(referencedColumns);
	}

	public Table getReferencedTable() {
		return this.referencedTable;
	}
	
	public List<Column> getReferencedColumns() {
		return this.referencedColumns;
	}
}
