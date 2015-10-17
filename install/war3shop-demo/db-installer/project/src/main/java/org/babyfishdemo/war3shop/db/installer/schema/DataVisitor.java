package org.babyfishdemo.war3shop.db.installer.schema;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public interface DataVisitor {

	void visitRow(Object[] dataItem) throws Exception;
}
