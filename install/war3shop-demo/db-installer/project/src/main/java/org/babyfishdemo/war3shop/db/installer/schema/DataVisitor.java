package org.babyfishdemo.war3shop.db.installer.schema;

/**
 * @author Tao Chen
 */
public interface DataVisitor {

	void visitRow(Object[] dataItem) throws Exception;
}
