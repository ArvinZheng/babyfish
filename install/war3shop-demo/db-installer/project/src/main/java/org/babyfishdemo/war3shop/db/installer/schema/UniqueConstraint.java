package org.babyfishdemo.war3shop.db.installer.schema;

import java.util.List;

/**
 * @author Tao Chen
 */
public class UniqueConstraint extends Constraint {

	UniqueConstraint(List<Column> columns) {
		super(columns);
	}
}
