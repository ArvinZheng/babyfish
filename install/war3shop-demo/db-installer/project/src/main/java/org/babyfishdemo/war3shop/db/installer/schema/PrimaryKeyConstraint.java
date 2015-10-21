package org.babyfishdemo.war3shop.db.installer.schema;

import java.util.List;

/**
 * @author Tao Chen
 */
public class PrimaryKeyConstraint extends Constraint {

	PrimaryKeyConstraint(List<Column> columns) {
		super(columns);
	}

}
