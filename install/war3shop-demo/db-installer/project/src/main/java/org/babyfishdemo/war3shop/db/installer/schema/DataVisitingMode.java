package org.babyfishdemo.war3shop.db.installer.schema;

/**
 * @author Tao Chen
 */
public enum DataVisitingMode {

	ALL {
		@Override
		public boolean isVisitable(Column column) {
			return true;
		}
	},
	PK_AND_NON_LOB {
		@Override
		public boolean isVisitable(Column column) {
			return !column.getType().isLob();
		}
	},
	PK_AND_UNRESOLVED_LOB {
		@Override
		public boolean isVisitable(Column column) {
			if (column.getType().isLob()) {
				return true;
			}
			PrimaryKeyConstraint pkc = column.getTable().getPrimaryConstraint();
			if (pkc != null) {
				if (pkc.getColumns().contains(column)) {
					return true;
				}
			}
			return false;
		}
	};
	
	public abstract boolean isVisitable(Column column);
}
